package cqupt.third;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cqupt.third.adapter.GridPicListAdapter;
import cqupt.third.utils.ScreenUtil;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    // 返回码：系统图库
    private static final int CODE_IMAGE = 100;
    // 返回码：相机
    private static final int CODE_CAMERA = 200;
    // IMAGE TYPE
    private static final String IMAGE_TYPE = "image/*";
    // Temp照片路径
    public static String TEMP_IMAGE_PATH;
    private View mPopupView;
    private PopupWindow mPopupWindow;
    private GridView mGvPicList;
    private List<Bitmap> mPicList;
    // 主页图片资源ID
    private int[] mResPicId;
    // 显示Type
    private TextView mTvPuzzleMainTypeSelected;
    private LayoutInflater mLayoutInflater;
    private TextView mTvType2;
    private TextView mTvType3;
    private TextView mTvType4;
    // 游戏类型N*N
    private int mType = 2;
    // 本地图册、相机选择
    private String[] mCustomItems = new String[]{"本地图册", "相机拍照"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TEMP_IMAGE_PATH = Environment.getExternalStorageDirectory().getPath() + "/temp.png";
        mPicList = new ArrayList<>();
        // 初始化Views
        initViews();
        // 数据适配器
        mGvPicList.setAdapter(new GridPicListAdapter(mPicList, MainActivity.this));
        // Item点击监听
        mGvPicList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
                if (position == mResPicId.length - 1) {
                    // 选择本地图库 相机
                    showDialogCustom();
                } else {
                    // 选择默认图片
                    Intent intent = new Intent(MainActivity.this, PuzzleMain.class);
                    intent.putExtra("picSelectedID", mResPicId[position]);
                    intent.putExtra("mType", mType);
                    startActivity(intent);
                }
            }
        });

        /**
         * 显示难度Type
         */
        mTvPuzzleMainTypeSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 弹出popup window
                popupShow(v);
            }
        });

    }

    // 显示选择系统图库 相机对话框
    private void showDialogCustom() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("选择：");
        builder.setItems(mCustomItems, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (0 == which) {// 本地图册
                    //运行时权限检查
                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            && ContextCompat.checkSelfPermission(MainActivity.this,
                            READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        Intent intent = new Intent(Intent.ACTION_PICK, null);
                        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_TYPE);
                        startActivityForResult(intent, CODE_IMAGE);
                    } else {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    }
                } else if (1 == which) {// 系统相机
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    Uri photoUri = Uri.fromFile(new File(TEMP_IMAGE_PATH));
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    startActivityForResult(intent, CODE_CAMERA);
                }
            }
        });
        builder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Intent.ACTION_PICK, null);
                    intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_TYPE);
                    startActivityForResult(intent, CODE_IMAGE);
                } else {
                    Toast.makeText(MainActivity.this, "You denied the permission", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    /**
     * 调用图库相机回调方法
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == CODE_IMAGE && data != null) {
                // 相册
                Cursor cursor = this.getContentResolver().query(data.getData(), null, null, null, null);
                cursor.moveToFirst();
                String imagePath = cursor.getString(cursor.getColumnIndex("_data"));
                Intent intent = new Intent(MainActivity.this, PuzzleMain.class);
                intent.putExtra("picPath", imagePath);
                intent.putExtra("mType", mType);
                cursor.close();
                startActivity(intent);
            } else if (requestCode == CODE_CAMERA) {
                // 相机
                Intent intent = new Intent(MainActivity.this, PuzzleMain.class);
                intent.putExtra("picPath", TEMP_IMAGE_PATH);
                intent.putExtra("mType", mType);
                startActivity(intent);
            }
        }
    }

    private void popupShow(View view) {
        int density = (int) ScreenUtil.getDeviceDensity(this);
        mPopupWindow = new PopupWindow(mPopupView, 200 * density, 50 * density);
        mPopupWindow.setFocusable(true);
        mPopupWindow.setOutsideTouchable(true);
        Drawable transpent = new ColorDrawable(Color.TRANSPARENT);
        mPopupWindow.setBackgroundDrawable(transpent);

        int[] location = new int[2];
        view.getLocationOnScreen(location);
        mPopupWindow.showAtLocation(
                view,
                Gravity.NO_GRAVITY,
                location[0] - 40 * density,
                location[1] + 30 * density);
    }


    /**
     * 初始化Views
     */
    private void initViews() {
        mGvPicList = (GridView) findViewById(R.id.gv_xpuzzle_main_pic_list);
        // 初始化Bitmap数据
        mResPicId = new int[]{
                R.drawable.pic1, R.drawable.pic2, R.drawable.pic3,
                R.drawable.pic4, R.drawable.pic5, R.drawable.pic6,
                R.drawable.pic7, R.drawable.pic8, R.drawable.pic9,
                R.drawable.pic10, R.drawable.pic11, R.drawable.pic12,
                R.drawable.pic13, R.drawable.pic14,
                R.drawable.pic15, R.mipmap.ic_launcher};
        Bitmap[] bitmaps = new Bitmap[mResPicId.length];
        for (int i = 0; i < bitmaps.length; i++) {
            bitmaps[i] = BitmapFactory.decodeResource(getResources(), mResPicId[i]);
            mPicList.add(bitmaps[i]);
        }
        // 显示type
        mTvPuzzleMainTypeSelected = (TextView) findViewById(R.id.tv_puzzle_main_type_selected);
        mLayoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        // mType view
        mPopupView = mLayoutInflater.inflate(R.layout.xpuzzle_main_type_selected, null);
        mTvType2 = (TextView) mPopupView.findViewById(R.id.tv_main_type_2);
        mTvType3 = (TextView) mPopupView.findViewById(R.id.tv_main_type_3);
        mTvType4 = (TextView) mPopupView.findViewById(R.id.tv_main_type_4);
        // 监听事件
        mTvType2.setOnClickListener(this);
        mTvType3.setOnClickListener(this);
        mTvType4.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // Type
            case R.id.tv_main_type_2:
                mType = 2;
                mTvPuzzleMainTypeSelected.setText("2 X 2");
                mType = 2;
                break;
            case R.id.tv_main_type_3:
                mType = 3;
                mTvPuzzleMainTypeSelected.setText("3 X 3");
                mType = 3;
                break;
            case R.id.tv_main_type_4:
                mType = 4;
                mTvPuzzleMainTypeSelected.setText("4 X 4");
                mType = 4;
                break;
            default:
                break;
        }
        mPopupWindow.dismiss();
    }
}
