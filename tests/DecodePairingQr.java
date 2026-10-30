import com.google.zxing.*;
import com.google.zxing.common.HybridBinarizer;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;
import javax.imageio.ImageIO;

/** Invoked with piped stdout: pairing contents must stay in the caller's memory. */
public final class DecodePairingQr {
    public static void main(String[] args) throws Exception {
        BufferedImage image=ImageIO.read(new File(args[0]));
        int width=image.getWidth(),height=image.getHeight();
        BinaryBitmap bitmap=new BinaryBitmap(new HybridBinarizer(new RGBLuminanceSource(width,height,image.getRGB(0,0,width,height,null,0,width))));
        System.out.print(new MultiFormatReader().decode(bitmap,Map.of(DecodeHintType.TRY_HARDER,true)).getText());
    }
}
