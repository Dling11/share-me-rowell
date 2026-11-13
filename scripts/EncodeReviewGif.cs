using System;
using System.IO;
using System.Linq;
using System.Text;
using System.Windows.Media.Imaging;

// Build-time review artifact, using Windows' built-in encoder; never part of the app runtime.
class EncodeReviewGif {
    [STAThread] static int Main(string[] args){try{
        ushort delay=args.Length>2?ushort.Parse(args[2]):(ushort)10;if(delay==0)throw new ArgumentOutOfRangeException("delay");var encoder=new GifBitmapEncoder();foreach(string file in Directory.GetFiles(args[0],"frame-*.png").OrderBy(p=>p,StringComparer.Ordinal)){
            var image=new BitmapImage();image.BeginInit();image.CacheOption=BitmapCacheOption.OnLoad;image.UriSource=new Uri(Path.GetFullPath(file));image.EndInit();image.Freeze();var metadata=new BitmapMetadata("gif");metadata.SetQuery("/grctlext/Delay",(ushort)10);metadata.SetQuery("/grctlext/Disposal",(byte)2);encoder.Frames.Add(BitmapFrame.Create(image,null,metadata,null));
        }
        byte[] encoded;using(var buffer=new MemoryStream()){encoder.Save(buffer);encoded=buffer.ToArray();}
        int palette=(encoded[10]&128)!=0?3*(1<<((encoded[10]&7)+1)):0,offset=13+palette;
        // WIC can omit the requested delay from frame metadata. Set it on each real GIF block.
        int cursor=offset,controls=0;while(cursor<encoded.Length){byte tag=encoded[cursor++];if(tag==0x3b)break;if(tag==0x21){byte label=encoded[cursor++];if(label==0xf9){if(encoded[cursor]!=4)throw new InvalidDataException("Unexpected GIF control block");encoded[cursor+1]=(byte)((encoded[cursor+1]&0xe3)|8);encoded[cursor+2]=(byte)(delay&255);encoded[cursor+3]=(byte)(delay>>8);controls++;}while(encoded[cursor]!=0)cursor+=1+encoded[cursor];cursor++;}else if(tag==0x2c){byte flags=encoded[cursor+8];cursor+=9;if((flags&128)!=0)cursor+=3*(1<<((flags&7)+1));cursor++;while(encoded[cursor]!=0)cursor+=1+encoded[cursor];cursor++;}else throw new InvalidDataException("Unexpected GIF block");}if(controls!=encoder.Frames.Count)throw new InvalidDataException("Missing GIF frame timing");
        using(var output=File.Create(args[1])){output.Write(encoded,0,offset);var loop=new byte[]{0x21,0xff,0x0b}.Concat(Encoding.ASCII.GetBytes("NETSCAPE2.0")).Concat(new byte[]{3,1,0,0,0}).ToArray();output.Write(loop,0,loop.Length);output.Write(encoded,offset,encoded.Length-offset);}
        Console.WriteLine("Encoded "+encoder.Frames.Count+" native-renderer frames: "+args[1]);return 0;
    }catch(Exception error){Console.Error.WriteLine(error);return 1;}}
}
