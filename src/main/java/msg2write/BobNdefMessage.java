package msg2write;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import my_ownclass.LogUtil;
import my_ownclass.MyConstant;

import java.nio.charset.Charset;
import java.util.Locale;

/**
 * Created by Manso_he on 2015/3/3.
 */
public class BobNdefMessage {
    private static final String TAG_ASSIST= "[BobNdefMessage]-";

    @SuppressWarnings("all")
    public static NdefMessage getNdefMsg_from_RTD_URI(String uriFiledStr)
    {
        LogUtil.i(MyConstant.Tag, TAG_ASSIST + "into getNdefMsg_from_RTD_URI");
        NdefRecord rtdUriRecord = NdefRecord.createUri(uriFiledStr);
        return new NdefMessage(new NdefRecord[]{rtdUriRecord});
    }


    public static NdefMessage getNdefMsg_from_RTD_TEXT(String text, boolean encodeInUtf8)
    {
        LogUtil.i(MyConstant.Tag,TAG_ASSIST+"into getNdefMsg_from_RTD_TEXT");
        NdefRecord textRecord=createTextRecord(text,encodeInUtf8);
        return new NdefMessage(new NdefRecord[] { textRecord });
    }
    private static NdefRecord createTextRecord(String text, boolean encodeInUtf8)
    {
        Locale locale = new Locale("en","US");//a new Locale is created with US English
        byte[] langBytes = locale.getLanguage().getBytes(Charset.forName("US_ASCII"));
        Charset utfEncoding = encodeInUtf8 ? Charset.forName("UTF-8") :Charset.forName("UTF-16");
        int utfBit = encodeInUtf8 ? 0 :(1<<7);
        char status = (char) (utfBit+langBytes.length);
        //String text = "This is an RTD_TEXT exp";
        byte[] textBytes = text.getBytes(utfEncoding);
        byte[] data = new byte[1+langBytes.length+textBytes.length];//长度
        data[0] = (byte) status;// 复制状态标志位
        System.arraycopy(langBytes,0,data,1,langBytes.length);//复制语言吗
        System.arraycopy(textBytes,0,data,1+langBytes.length,textBytes.length);//复制实际文本信息
        NdefRecord textRecord = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,NdefRecord.RTD_TEXT,new byte[0],data);
        return textRecord;
    }

//    //
//    public static NdefMessage getNdefMsg_from_ABSOLUTE_URI(String absoluteUri)
//    {
//        LogUtil.i(MyConstant.Tag,TAG_ASSIST+"into getNdefMsg_from_ABSOLUTE_URI");
//        byte[] absoluteUriByte = absoluteUri.getBytes(Charset.forName("US_ASCII"));
//        NdefRecord uriRecord = new NdefRecord(NdefRecord.TNF_ABSOLUTE_URI,absoluteUriByte,new byte[0],new byte[0]);
//        return new NdefMessage(new NdefRecord[]{uriRecord});
//    }

    public static NdefMessage getNdefMsg_from_EXTERNAL_TYPE(String payload)
    {
        LogUtil.i(MyConstant.Tag,TAG_ASSIST+"into getNdefMsg_from_EXTERNAL_TYPE");
        byte[] payLoadBytes = payload.getBytes();
        String domain = "com.nuaa.manso_he.nfctest1";
        String type = "externalType";
        String externalType= domain +":"+ type;
        //method1 :Creating the NdefRecord manually
        NdefRecord externalRecord1 = new NdefRecord(NdefRecord.TNF_EXTERNAL_TYPE,externalType.getBytes(),new byte[0],payLoadBytes);
        return new NdefMessage(new NdefRecord[]{externalRecord1});
    }

    public static NdefMessage getNdefMsg_from_MIME_MEDIA(String payload,String mimeType, boolean flagAddAAR)
    {
        LogUtil.i(MyConstant.Tag,TAG_ASSIST+"into getNdefMsg_from_MIME_TYPE");
        byte[] payLoadBytes = payload.getBytes(Charset.forName("US_ASCII"));
        //method1
        NdefRecord mimeRecord1 =  new NdefRecord(NdefRecord.TNF_MIME_MEDIA,mimeType.getBytes(Charset.forName("US_ASCII")),new byte[0],payLoadBytes);
        //the identifier of the record is given as 0, since it will be the first record in the NdefMessage

        //method2
        NdefRecord mimeRecord2 = NdefRecord.createMime(mimeType,payLoadBytes);
        return new NdefMessage(new NdefRecord[]{mimeRecord1});
    }

    private static final BiMap<Byte, String>URI_PREFIX_MAP = ImmutableBiMap.<Byte,String>builder()
            .put((byte)0x00,"").put((byte)0x01,"http://www.").put((byte)0x02,"https://www.")
            .put((byte)0x03,"http://").put((byte)0x04,"https://").put((byte)0x05,"tel:")
            .put((byte)0x06,"mailto:").put((byte)0x07,"ftp://anonymous:anonymous@").put((byte)0x08,"ftp://ftp.")
            .put((byte)0x09,"ftps://").put((byte)0x0A,"sftp://").put((byte)0x0B,"smb://")
            .put((byte)0x0C,"nfs://").put((byte)0x0D,"ftp://").put((byte)0x0E,"dav://")
            .put((byte)0x0F,"news:").put((byte)0x10,"telnet://").put((byte)0x11,"imap:")
            .put((byte)0x12,"rtsp://").put((byte)0x13,"urn:").put((byte)0x14,"pop:")
            .put((byte)0x15,"sip:").put((byte)0x16,"sips:").put((byte)0x17,"tftp:")
            .put((byte)0x18,"btspp://").put((byte)0x19,"bt12cap://").put((byte)0x1A,"btgoep://")
            .put((byte)0x1B,"tcpodex://").put((byte)0x1C,"irdaobex://").put((byte)0x1D,"file://")
            .put((byte)0x1E,"urn:epc:id:").put((byte)0x1F,"urn:epc:tag:").put((byte)0x20,"urn:epc:pat:")
            .put((byte)0x21,"urn:epx:raw:").put((byte)0x22,"urn:epc:").put((byte) 0x23, "urn:nfc:").build();

}
