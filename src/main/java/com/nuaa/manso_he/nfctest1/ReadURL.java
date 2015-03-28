package com.nuaa.manso_he.nfctest1;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Parcelable;
import android.provider.Settings;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

import my_ownclass.LogUtil;
import my_ownclass.MyConstant;


public class ReadURL extends Activity {
    //Log的查询标志
    private static final String Tag_ASSIST = "[Read]";
    private Context mContext;
    //NFC Declarations
    private NfcAdapter mNfcAdapter = null;
    private PendingIntent mNfcPendingIntent = null;
    //UI
    private TextView mTitle = null;
    private TextView mId=null;
    private TextView mPayload = null;
    private AlertDialog dialog =null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_url);
        LogUtil.i(MyConstant.Tag, Tag_ASSIST + "into onCreat");
        mContext=this;

        //NFC Check
        checkNFCFunction();
        //Init UI
        intiUI();
        //Init NFC
        initNFC();
    }

    private void checkNFCFunction() {
        //getting the default NFC adapter
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        //check the NFC adapter first
        if(mNfcAdapter == null)
        {
            //mTextView.setText("NFC adapter is not available");

            LogUtil.i(MyConstant.Tag,Tag_ASSIST+"into nullNfcAdapter");


            AlertDialog.Builder customBuilder;
            customBuilder = new AlertDialog.Builder(mContext);
            customBuilder.setTitle(getString(R.string.inquire)).setMessage(getString(R.string.nfc_notice2))
                    .setNegativeButton(getString(R.string.no),new
                            DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialog.dismiss();
                                    finish();
                                }
                            }).setPositiveButton(getString(R.string.yes),new
                    DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialog.dismiss();
                            finish();
                        }
                    });
            dialog=customBuilder.create();
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            SetDialogWidth(dialog).show();

            return;
        }else
        {
            if(!mNfcAdapter.isEnabled())
            {

                LogUtil.i(MyConstant.Tag,Tag_ASSIST+"into nullNfcAdapter");
                AlertDialog.Builder customBuilder = new AlertDialog.Builder(mContext);

                customBuilder.setTitle(getString(R.string.inquire)).setMessage(getString(R.string.nfc_notice3))
                        .setNegativeButton(getString(R.string.no),new
                                DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialog.dismiss();
                                        finish();
                                    }
                                }).setPositiveButton(getString(R.string.yes),new
                        DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialog.dismiss();
                                Intent setnfc = new Intent(Settings.ACTION_NFC_SETTINGS);
                                startActivity(setnfc);
                            }
                        });
                dialog= customBuilder.create();
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                SetDialogWidth(dialog).show();
                return;
            }
        }

    }

    private void intiUI() {
        //TODO Auto-generated method stub
        mPayload = (TextView) findViewById(R.id.tvACT2_1);
        mTitle = (TextView) findViewById(R.id.tvACT2);
        mId=(TextView)findViewById(R.id.tvACT3);
    }

    private void initNFC() {
        LogUtil.i(MyConstant.Tag,Tag_ASSIST+"into initNFC");
        //getting the default NFC adapter
        mNfcAdapter =NfcAdapter.getDefaultAdapter(this);
        mNfcPendingIntent =PendingIntent.getActivity(
                this,0,new Intent(this,getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),0);
        //FLAG_ACTIVITY_SINGLE_TOP:not creating multiple instances of the same application.

    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle.setText(title);
    }

    @Override
    protected void onResume() {
        //TODO Auto-generated method stub
        super.onResume();
        //NFC Check
        checkNFCFunction();
        enableForegroundDispatch();
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        //消息判别
        if(NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction()))
        {
            LogUtil.i(MyConstant.Tag,Tag_ASSIST+"ACTION_NDEF_DISCOVERED");
        }
        else if(NfcAdapter.ACTION_TECH_DISCOVERED.equals(getIntent().getAction()))
        {
            LogUtil.i(MyConstant.Tag, Tag_ASSIST+"ACTION_TECT_DISCOVERED");

        }
        else if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(getIntent().getAction()))
        {
            LogUtil.i(MyConstant.Tag, Tag_ASSIST+"ACTION_TAG_DISCOVERED");
        }
        resolveIntent(getIntent());
    }

    @Override
    protected void onPause() {
        super.onPause();
        disableForeGroundDispatch();
    }

    //16进制转换成字符串
    public static String bytesToHexString(byte[] bArray) {
        StringBuffer sb = new StringBuffer(bArray.length);
        String sTemp;
        for (int i = 0; i < bArray.length; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }

    //从Intent中获取NDEF消息
    private void resolveIntent(Intent intent) {
        LogUtil.i(MyConstant.Tag, Tag_ASSIST+"into resolveIntent");
        String action = intent.getAction();
        if(NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action) ||NfcAdapter.ACTION_TECH_DISCOVERED.equals(action) ||NfcAdapter.ACTION_TAG_DISCOVERED.equals(action))
        {
            Tag tag=intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            byte[] tagId=tag.getId();
            NdefMessage[] messages = null;
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if(rawMsgs !=null)
            {
                messages = new NdefMessage[rawMsgs.length];
                for(int i=0;i<rawMsgs.length;i++)
                {
                    messages[i]=(NdefMessage)rawMsgs[i];
                    LogUtil.i(MyConstant.Tag,Tag_ASSIST+"messages["+i+"] = "+messages[i]);
                }
            }
            else
            {
                //Unknown tag type
                byte[] empty =new byte[]{};
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN,empty,empty,empty);
                NdefMessage msg= new NdefMessage(new NdefRecord[]{record});
                messages = new NdefMessage[]{msg};
            }
            //Setup the views
            setTitle(R.string.title_scanned_tag_NDEF);
            mId.setText("ID:"+bytesToHexString(tagId));
            //process NDEF Msg
            processNDEFMsg(messages);
        }
        else
        {
            LogUtil.e(MyConstant.Tag, Tag_ASSIST+"Unknown intent "+intent);
            finish();
            return;
        }
    }


    //获取待解析的NDEFMessage
    private void processNDEFMsg(NdefMessage[] messages) {
        LogUtil.i(MyConstant.Tag,Tag_ASSIST+"into processNDEFMsg");
        if(messages == null || messages.length==0)
        {
            LogUtil.e(MyConstant.Tag,Tag_ASSIST+"NdefMessgae is null");
            Toast.makeText(mContext,"this Tag is null!",Toast.LENGTH_SHORT).show();
            return;
        }
        for(int i=0;i<messages.length;i++)
        {
            int length =messages[i].getRecords().length;
            LogUtil.i(MyConstant.Tag,Tag_ASSIST+"Message "+(i+1)+","+"length="+length);
            Toast.makeText(mContext,"Read Succeed!",Toast.LENGTH_SHORT).show();
            NdefRecord[] records= messages[i].getRecords();
            for(int j=0;j<length;j++)  //几个记录
            {
                for(NdefRecord record:records)
                {
                    parseRecord(record);
                }
            }
        }
    }

    //解析NdefMessage
    private void parseRecord(NdefRecord record) {
        LogUtil.i(MyConstant.Tag,Tag_ASSIST+"into parseRecord");
        //判断标签的类型名称格式（TNF）
        short tnf = record.getTnf();
        switch (tnf)
        {
            case NdefRecord.TNF_WELL_KNOWN:
                parseWellKnown(record);
                LogUtil.i(MyConstant.Tag,Tag_ASSIST+"into parseWellKnownRecord");
                break;
            case NdefRecord.TNF_ABSOLUTE_URI:
                parseAbsoluteUriRecord(record);
                LogUtil.i(MyConstant.Tag,Tag_ASSIST+"into parseAbsoluteUriRecord");
                break;
            case NdefRecord.TNF_MIME_MEDIA:
                parseMimeMediaRecord(record);
                LogUtil.i(MyConstant.Tag,Tag_ASSIST+"into parseMimeMediaRecord");
                break;
            case NdefRecord.TNF_EXTERNAL_TYPE:
                parseExternalTypeRecord(record);
                LogUtil.i(MyConstant.Tag,Tag_ASSIST+"into parseExternalTypeRecord");
                break;
            default:
                LogUtil.e(MyConstant.Tag,Tag_ASSIST+ "UnKnown TNF"+ tnf);break;
        }
    }
    //简单解析TNF_EXTERNAL_TYPE
    private void parseExternalTypeRecord(NdefRecord record) {
        byte[] payload=record.getPayload();
        Uri uri= Uri.parse(new String(payload, Charset.forName("UTF-8")));
        mPayload.setText("External:"+uri);
    }
    //简单解析TNF_MIME_MEDIA
    private void parseMimeMediaRecord(NdefRecord record)
    {
        byte[] payload=record.getPayload();
        Uri uri= Uri.parse(new String(payload, Charset.forName("UTF-8")));
        mPayload.setText("MIME:"+uri);
    }
    //解析TNF_ABSOLUTE_URI
    private void parseAbsoluteUriRecord(NdefRecord record)
    {

        LogUtil.i(MyConstant.Tag,Tag_ASSIST+"into parseAbsolute");
        byte[] payload = record.getPayload();
        Uri uri = Uri.parse(new String(payload, Charset.forName("UTF-8")));
        //1
        LogUtil.i(MyConstant.Tag,Tag_ASSIST+"the Record Tnf:"+record.getTnf()+"\n");
        //T
        LogUtil.i(MyConstant.Tag,Tag_ASSIST+"the Record Type:"+new String(record.getType())+"\n");
        LogUtil.i(MyConstant.Tag,Tag_ASSIST+"the Record ID:"+ new String(record.getId())+"\n");
        LogUtil.i(MyConstant.Tag,Tag_ASSIST+"the Record payload:"+uri+"\n");
        mPayload.setText("REV:"+uri);
    }
    //解析TNF_WELL_KNOWN
    private void parseWellKnown(NdefRecord record)
    {
        byte[] type =  record.getType();
        if(Arrays.equals(type,NdefRecord.RTD_URI))
        {
            parseWellKnownURIRecord(record);
        }else if (Arrays.equals(type,NdefRecord.RTD_TEXT))
        {
            parseWellKnownTEXTRecord(record);
        }else if(Arrays.equals(type, NdefRecord.RTD_SMART_POSTER))
        {
            LogUtil.i(MyConstant.Tag, Tag_ASSIST + "RTD_SMART_POSTER(can't be parsed)");
            Toast.makeText(mContext,"The wellkonwn record can't be parsed.",Toast.LENGTH_SHORT).show();
        }else if(Arrays.equals(type,NdefRecord.RTD_HANDOVER_CARRIER))
        {
            LogUtil.i(MyConstant.Tag,Tag_ASSIST+"RTD_HANDOVER_CARRIER(can't be parsed)");
            Toast.makeText(mContext,"The wellkonwn record can't be parsed.",Toast.LENGTH_SHORT).show();
        }else if(Arrays.equals(type,NdefRecord.RTD_HANDOVER_REQUEST))
        {
            LogUtil.i(MyConstant.Tag,Tag_ASSIST+"RTD_HANDOVER_REQUEST(can't be parsed)");
            Toast.makeText(mContext,"The wellkonwn record can't be parsed.",Toast.LENGTH_SHORT).show();
        }else if(Arrays.equals(type,NdefRecord.RTD_HANDOVER_SELECT))
        {
            LogUtil.i(MyConstant.Tag,Tag_ASSIST+"RTD_HANDOVER_SELECT(can't be parsed)");
            Toast.makeText(mContext,"The wellkonwn record can't be parsed.",Toast.LENGTH_SHORT).show();
        }else if(Arrays.equals(type,NdefRecord.RTD_ALTERNATIVE_CARRIER))
        {
            LogUtil.i(MyConstant.Tag,Tag_ASSIST+"RTD_ALTERNATIVE_CARRIER(can't be parsed)");
            Toast.makeText(mContext,"The wellkonwn record can't be parsed.",Toast.LENGTH_SHORT).show();
        }
    }


    //解析TNF_WELL_KNOWN中的RTD_URI
    private void parseWellKnownURIRecord(NdefRecord record)
    {
        //Log的输出
        LogUtil.i(MyConstant.Tag,Tag_ASSIST+"into parseWellKnown(URI)");
        //接收payload
        byte[] payload = record.getPayload();
        //判断前缀
        String prefix = URI_PREFIX_MAP.get(payload[0]);
        //连接前缀和payload内容
        byte[] fullUri = concat(prefix.getBytes(Charset.forName("UTF-8")), Arrays.copyOfRange(payload, 1, payload.length));
        //将二进制转换成URI类型
        Uri uri = Uri.parse(new String(fullUri,Charset.forName("UTF-8")));

        //输出前缀
        LogUtil.i(MyConstant.Tag,Tag_ASSIST+"the prefix: "+ prefix +"\n");
        //输出TNF（Type Name Format）类型
        LogUtil.i(MyConstant.Tag,Tag_ASSIST+"the Record Tnf:"+record.getTnf()+"\n");
        //输出Type（在此为U）
        LogUtil.i(MyConstant.Tag,Tag_ASSIST+"the Record Type:"+new String(record.getType())+"\n");
        //输出ID
        LogUtil.i(MyConstant.Tag,Tag_ASSIST+"the Record ID:"+ new String(record.getId())+"\n");
        //输出Record的记录
        LogUtil.i(MyConstant.Tag,Tag_ASSIST+"the Record payload:"+uri+"\n");
        //在界面上输出Payload的内容
        mPayload.setText("REV:"+uri);
    }

    //解析TNF_WELL_KNOWN的RTD_TEXT
    private void parseWellKnownTEXTRecord(NdefRecord record)
    {
        //Log的输出
        LogUtil.i(MyConstant.Tag,Tag_ASSIST+"into parseWellKnown(TEXT)");
        String payloadStr="";
        //获取payload
        byte[] payload=record.getPayload();
        //获取记录payload第一个字节
        Byte statusByte =record.getPayload()[0];
        String textEncoding ="";
        //获取状态字节码
        textEncoding=((statusByte & 0200)==0)?"UTF-8":"UTF-16";
        int languageCodeLength=0;
        //获取语言码的长度
        languageCodeLength= statusByte & 0077;
        String languageCode="";
        languageCode=new String(payload,1,languageCodeLength,Charset.forName("UTF-8"));
        //获取payload的实际数据
        try{
            payloadStr= new String(payload,languageCodeLength+1,payload.length-languageCodeLength-1,textEncoding);
            mTitle.setText("Text:"+payloadStr);
        }catch(UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }

        //打印状态字节码
        LogUtil.i(MyConstant.Tag,Tag_ASSIST+"textEncoding ="+ textEncoding);
        //打印语言码信息
        LogUtil.i(MyConstant.Tag,Tag_ASSIST+"languageCodeLength ="+ languageCodeLength+"\n languageCode"+languageCode);
        //打印TNF信息
        LogUtil.i(MyConstant.Tag,Tag_ASSIST+"the Record TNF:"+record.getTnf());
        //打印Type信息
        LogUtil.i(MyConstant.Tag,Tag_ASSIST+"the Record Type:"+new String(record.getType()));
        //打印ID信息
        LogUtil.i(MyConstant.Tag,Tag_ASSIST+"ID:="+new String(record.getId())+"\n");
        //打印payload信息
        LogUtil.i(MyConstant.Tag,Tag_ASSIST+"the Record payload:"+payloadStr);
    }



    //二进制串连接函数
    private byte[] concat(byte[] bytes, byte[] bytes1) {
        byte[] bufret=null;
        int len1=0;
        int len2=0;
        if(bytes!=null)
            len1=bytes.length;
        if(bytes1!=null)
            len2=bytes1.length;
        if(len1+len2>0)
            bufret=new byte[len1+len2];
        if(len1>0)
            System.arraycopy(bytes,0,bufret,0,len1);
        if(len2>0)
            System.arraycopy(bytes1,0,bufret,len1,len2);
        return bufret;
    }
    /*
    NFC Forum "URI Record Type Definition"

    This is a mapping of "URI Identifier Codes" to URI string prefix,
    per section 3.2.2 of the NFC Forum URI Record Type Definition document.
     */
    private static final BiMap<Byte,String> URI_PREFIX_MAP = ImmutableBiMap.<Byte,String>builder()
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
            .put((byte)0x21,"urn:epx:raw:").put((byte)0x22,"urn:epc:").put((byte)0x23,"urn:nfc:").build();

    //打开NFC前台调度功能
    private void enableForegroundDispatch() {
        if(mNfcAdapter!=null)
        {
            mNfcAdapter.enableForegroundDispatch(this,mNfcPendingIntent,null,null);
        }
    }

    //关闭前台调度功能
    private void disableForeGroundDispatch() {
        if(mNfcAdapter!=null)
        {
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }


    private Dialog SetDialogWidth(Dialog dialog) {
        DisplayMetrics dm=new DisplayMetrics();
        //取得窗口属性
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        //取得窗口的宽度
        int screenWidth = dm.widthPixels;
        //窗口高度
        int screenHeight = dm.heightPixels;
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        if(screenWidth>screenHeight)
        {
            params.width = (int)(((float)screenHeight)*0.875);
        }else
        {
            params.width = (int)(((float)screenWidth)*0.875);
        }
        dialog.getWindow().setAttributes(params);
        return dialog;
    }


}
