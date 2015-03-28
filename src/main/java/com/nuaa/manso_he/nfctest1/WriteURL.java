package com.nuaa.manso_he.nfctest1;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.AsyncTask;
import android.provider.Settings;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import msg2write.BobNdefMessage;
import my_ownclass.LogUtil;
import my_ownclass.MyConstant;


public class WriteURL extends Activity {

    private static final String Tag_ASSIST = "[WriteTag_RTDUrl-Book]";
    private Context mContext;
    //NFC Declaration
    private NfcAdapter mNfcAdapter = null;
    private IntentFilter[] mWriteTagFilters = null;
    private PendingIntent mNfcPendingIntent = null;
    private String[][] mTechList = null;
    private NdefMessage NDEFMsg2Write = null;
    //UI
    private EditText mEditText = null;
    private String payloadStr = null;
    private AlertDialog alertDialog = null;
    private AlertDialog dialog=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_url);
        LogUtil.i(MyConstant.Tag, Tag_ASSIST + "into onCreat");
        mContext = this;
        checkNFCFunction();//NFC Check
        initUI();//Init UI
        initNFC();//Init NFC
    }

    private void initUI()
    {
        mEditText = (EditText) findViewById(R.id.write_tag_url_et);
        findViewById(R.id.write_tag_url_btn).setOnClickListener(mTagWriter);
        mEditText.addTextChangedListener(mTextWatch);
        mEditText.setText("www.baidu.com");
    }

    private TextWatcher mTextWatch = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            LogUtil.i(MyConstant.Tag,Tag_ASSIST+"onTextChange:"+i);
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            LogUtil.i(MyConstant.Tag,Tag_ASSIST+"beforeTextChanged:"+i);
        }

        @Override
        public void afterTextChanged(Editable editable) {
            payloadStr=editable.toString();
            LogUtil.i(MyConstant.Tag,Tag_ASSIST+"afterTextChanged:"+editable);
            LogUtil.i(MyConstant.Tag,Tag_ASSIST+"afterTextChanged,payloadStr="+payloadStr);
        }
    };


    private View.OnClickListener mTagWriter = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //Write to a tag for as long as the dialog is shown.
            enableForegroundDispatch();
            //获得AlertDialog.Builder实例并配置参数
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("Touch Tag to Write")
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            LogUtil.i(MyConstant.Tag,Tag_ASSIST+"mTagWriter - onCancel");
                            disableForegroundDispatch();
                        }
                    });
            alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
        }
    };

    private void enableForegroundDispatch()
    {
        if(mNfcAdapter != null)
        {
            mNfcAdapter.enableForegroundDispatch(this,mNfcPendingIntent,mWriteTagFilters,mTechList);
        }
    }

    private void disableForegroundDispatch()
    {
        if(mNfcAdapter != null)
        {
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }

    private void initNFC()
    {
        LogUtil.i(MyConstant.Tag,Tag_ASSIST+"[WriteTagUrl]into NFC_Init");
        mNfcPendingIntent = PendingIntent.getActivity(this,0, new Intent(this,getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP),0);
        //FLAG_ACTIVITY_SINGLE_TOP: not creating multiple instances of the same application
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter techDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        //Intent filters for writing to a tag
        mWriteTagFilters = new IntentFilter[]
                {
                        techDetected
                };//just trying to find a tag,not ndef or tech
        mTechList = new String[][]{
                new String[]
                        {
                                Ndef.class.getName()
                        },
                new String[]
                        {
                                NdefFormatable.class.getName()
                        }
        };
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(alertDialog != null)
            alertDialog.cancel();
        disableForegroundDispatch();//disable foreground dispatch.
    }

    @Override
    protected void onResume() {
        //TODO Auto-generated method stub
        super.onResume();
        enableForegroundDispatch();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // super.onNewIntent(intent);
        setIntent(intent);
        resolveIntent(intent);
    }

    private void resolveIntent(Intent intent){
        //由于采用了Foreground Dispatch,故无需再采用Intent过滤
        if(NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction()))
        {
            LogUtil.i(MyConstant.Tag,Tag_ASSIST+"ACTION_NDEF_DISCOVERED");
        }else if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction()))
        {
            LogUtil.i(MyConstant.Tag,Tag_ASSIST+"ACTION_TAG_DISCOVERED");
        }else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction()))
        {
            LogUtil.i(MyConstant.Tag,Tag_ASSIST+"ACTION_TECT_DISCOVERED");
        }

        //get NFC object
        Tag detectTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if(detectTag == null)LogUtil.i(MyConstant.Tag,Tag_ASSIST+"null");
        else
        {
            //validate that this tag can be written
            if(supportedTechs(detectTag.getTechList()))
            {
                NDEFMsg2Write = BobNdefMessage.getNdefMsg_from_RTD_URI(payloadStr);
                new WriteTask(this,NDEFMsg2Write,detectTag).execute();
            }else {
                Toast.makeText(mContext, "This tag type is not supported", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static boolean supportedTechs(String[] techs)
    {
        LogUtil.i(MyConstant.Tag,Tag_ASSIST+"into supportedTechs");
        for(String s :techs)
        {
            LogUtil.i(MyConstant.Tag,Tag_ASSIST+"all supportedTechs =" + s);
        }
        boolean ultralight =false;
        boolean nfcA =false;
        boolean ndef =false;
        for(String tech :techs)
        {
            if(tech.equals("android.nfc.tech.MifareUltralight"))
            {
                LogUtil.i(MyConstant.Tag,Tag_ASSIST+"supportedTechs is:ultralight");
            }else if (tech.equals("android.nfc.tech.NfcA"))
            {
                nfcA = true;
                LogUtil.i(MyConstant.Tag,Tag_ASSIST+"supportedTechs is:NfcA");
            }else if(tech.equals("android.nfc.tech.Ndef") || tech.equals("android.nfc.tech.NdefFormatable"))
            {
                ndef = true;
                LogUtil.i(MyConstant.Tag,Tag_ASSIST+"supportedTechs is:Ndef/NdefFormatable");
            }else if (tech.equals("android.nfc.tech.MifareClassic"))
            {
                LogUtil.i(MyConstant.Tag,Tag_ASSIST+"supportedTechs is:MifareClassic");
            }
        }
        if(ndef)  //only ndef
        {
            return true;
        }else
        {
            return false;
        }
    }

    private void checkNFCFunction()
    {
        //TODO Auto-generated method stub
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

        LogUtil.i(MyConstant.Tag,Tag_ASSIST+"Succeed");
    }

    private Dialog SetDialogWidth(Dialog dialog) {
        DisplayMetrics dm=new DisplayMetrics();
        //取得窗口属性
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        //取得窗口的宽度
        int screenWidth = dm.widthPixels;
        //窗口高度
        int screenHeight =dm.heightPixels;
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

    static class WriteTask extends AsyncTask<Void,Void,Void>
    {
        Activity host = null;
        NdefMessage msg = null;
        Tag tag = null;
        String text = null;

        WriteTask(Activity host,NdefMessage msg,Tag tag)
        {
            LogUtil.i(MyConstant.Tag,Tag_ASSIST+"into WriteTask AsyncTask");
            this.host=host;
            this.msg=msg;
            this.tag= tag;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            int size = msg.toByteArray().length;
            try
            {
                Ndef ndef = Ndef.get(tag);
                if(ndef ==null)
                {
                    NdefFormatable formatable = NdefFormatable.get(tag);
                    if(formatable !=null)
                    {
                        try
                        {
                            formatable.connect();
                            try
                            {
                                formatable.format(msg);
                            }catch (Exception e) {
                                text = "Failed to format tag, Tag refused to format";
                            }
                        }catch (Exception e)
                        {
                            text= "Failed to connect tag,Tag refused to connect";
                        }finally {
                            formatable.close();
                        }
                    }else
                    {
                        text="NDEF is not support in this Tag";
                    }
                }else
                {
                    ndef.connect();
                    try
                    {
                        if(!ndef.isWritable())
                        {
                            text= "Tag is read-only";
                        }else if(ndef.getMaxSize()<size)
                        {
                            text= "The data cannot written to tag, Message is too big for tag, Tag capacity is"
                                    +ndef.getMaxSize()+"bytes, message is" +size +"bytes.";
                        }else
                        {
                            ndef.writeNdefMessage(msg);
                            text="Message is written tag, message="+ msg;
                        }
                    }catch (Exception e)
                    {
                        text= "Tag refused to connect";
                    }finally {
                        ndef.close();
                    }
                }
            }catch (Exception e)
            {
                text= "Write operation is failed,General exception:"+ e.getMessage();
                LogUtil.i(MyConstant.Tag,Tag_ASSIST+"Exception when writing tag, Write operation is failed"+text);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(text != null)
            {
                Toast.makeText(host,text,Toast.LENGTH_SHORT).show();
            }
            host.finish();
        }
    }
}
