import com.google.zxing.*;
import com.google.zxing.common.HybridBinarizer;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import javax.imageio.ImageIO;

/** Decode a rendered QR, including its displayed size, using the Android scanner's decoder. */
public final class QrImageCheck {
    public static void main(String[] args) throws Exception {
        BufferedImage image=ImageIO.read(new File(args[0]));
        int width=image.getWidth(),height=image.getHeight();
        int[] pixels=image.getRGB(0,0,width,height,null,0,width);
        BinaryBitmap bitmap=new BinaryBitmap(new HybridBinarizer(new RGBLuminanceSource(width,height,pixels)));
        String decoded=new MultiFormatReader().decode(bitmap,Map.of(DecodeHintType.TRY_HARDER,true)).getText();
        String expected=Files.readString(Path.of(args[1])).trim();
        if(!expected.equals(decoded))throw new AssertionError("Displayed QR differs from pairing session");
        System.out.println("QR image decoded and matches pairing session: "+new File(args[0]).getName());
    }
}
