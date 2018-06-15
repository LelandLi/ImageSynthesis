package com.lelandli.imagesynthesis
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.app.ProgressDialog.show
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.support.v4.app.ActivityCompat
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.os.Build
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore.Images.Media.getBitmap
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_identity_authentication.*
import java.io.File
import com.nanchen.compresshelper.CompressHelper
import org.jetbrains.anko.*
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


/**
 * 当前类注释:身份认证页面
 * 项目名: NewFace
 * 作者: Leland on 2018/1/9
 */
class IdentityAuthenticationActivity : AppCompatActivity() {
    private var dialog: Dialog? = null
    //第一张图片
    private var firstBitmap:Bitmap? = null
    //第二张图片
    private var secondBitmap:Bitmap? = null
    private var imageUri: Uri ? = null
    private val fileUri = File(Environment.getExternalStorageDirectory().path + "/photo.jpg")
    //回调参数设置
    private val CODE_GALLERY_REQUEST = 0xa0
    private val CODE_CAMERA_REQUEST = 0xa1
    private val CAMERA_PERMISSIONS_REQUEST_CODE = 0x03
    private val STORAGE_PERMISSIONS_REQUEST_CODE = 0x04
    private var toast: Toast? = null
    private var nameNumbers :String? =""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_identity_authentication)
        initData()
    }

    private fun initData() {
        identity_submit.setOnClickListener {
            //提交信息
            if (firstBitmap == null || secondBitmap == null){
                showToastMsgLong("提交前，请完善信息。")
                return@setOnClickListener
            }
            nameNumbers = nameNumber.text.toString().trim()
            //提交信息
            val bitmaps = mergeBitmap(firstBitmap!!, secondBitmap!!)
            if (bitmaps != null)
                previewSynthesis.setImageBitmap(bitmaps)
            val dialog :ProgressDialog =indeterminateProgressDialog("正在生成图片。。。");//
            dialog.show()
            Thread {
                val folder = Environment.getExternalStorageDirectory().toString() + File.separator + "images" + File.separator
                val appDir = File(folder)
                if (!appDir.exists()) {
                    appDir.mkdirs()
                }
                val fileName = "LJL"+System.currentTimeMillis()+ ".jpg"
                val file = File(appDir, fileName)
                try {
                    val fos = FileOutputStream(file)
                    bitmaps.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                    fos.flush()
                    fos.close()
                    runOnUiThread({
                        toast("图片保存成功")
                        dialog.dismiss() })
                } catch (e: Exception) {
                    runOnUiThread({
                        alert("图片保存错误，请重新保存") {
                                 title("提示")
                                 yesButton {  }
                                 noButton { }
                             }.show()
                        dialog.dismiss() })

                }
            }.start()
        }
        //第一张图片删除
        idcard_delete_image1.setOnClickListener {
            if (firstBitmap != null){
                firstBitmap!!.recycle()
                firstBitmap = null
                id_caid_oneimage.setImageResource(R.drawable.photo_frame_image)
                idcard_delete_image1.visibility = View.INVISIBLE
                if (secondBitmap == null){
                    idcard_twoimage_layout.visibility = View.GONE
                }
            }
        }
        //第二张图片删除
        idcard_delete_image2.setOnClickListener {
            if (secondBitmap != null){
                secondBitmap!!.recycle()
                secondBitmap = null
                id_caid_twoimage.setImageResource(R.drawable.photo_frame_image)
                idcard_delete_image2.visibility = View.INVISIBLE
                if (firstBitmap == null){
                    idcard_twoimage_layout.visibility = View.GONE
                }
            }
        }
        //第一张图片
        id_caid_oneimage.setOnClickListener {
            if (firstBitmap == null){
                //调用相册相机
                showPhotoDialog()
            }
        }
        // 第二张图片
        id_caid_twoimage.setOnClickListener {
            if (secondBitmap == null){
                //调用相册相机
                showPhotoDialog()
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun showPhotoDialog() {
        dialog = null
        val builder = AlertDialog.Builder(this, R.style.remind_dialog)
        //        builder.setCancelable(false); //点击dialog以为的地方不消失
        val inflater = LayoutInflater.from(this@IdentityAuthenticationActivity)
        val mInflate = inflater.inflate(R.layout.photo_selection_dialog, null)
        //拍照上传
        mInflate.findViewById<View>(R.id.dialog_photograph).setOnClickListener {
            autoObtainCameraPermission()
            dialog!!.dismiss()
            dialog = null
        }
        //相册选择
        mInflate.findViewById<View>(R.id.dialog_album).setOnClickListener {
            autoObtainStoragePermission()
            dialog!!.dismiss()
            dialog = null
        }
        mInflate.findViewById<View>(R.id.dialog_cancel).setOnClickListener {
            dialog!!.dismiss()
            dialog = null
        }
        dialog = builder.create()
        (dialog as AlertDialog?)!!.show()
        //添加动画效果
        val window = (dialog as AlertDialog?)!!.window
        window!!.setGravity(Gravity.BOTTOM)
        window.setWindowAnimations(R.style.AnimBottom)
        window.setContentView(mInflate)
        val windowManager = windowManager
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val lp = (dialog as AlertDialog?)!!.window!!.attributes
        lp.width = size.x //设置宽度
        (dialog as AlertDialog?)!!.window!!.attributes = lp
    }

    /**
     * 自动获取相机权限
     */
    private fun autoObtainCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                showToastMsgLong("您已经拒绝过一次")
            }
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE), CAMERA_PERMISSIONS_REQUEST_CODE)
        } else {//有权限直接调用系统相机拍照
            if (hasSdcard()) {
                imageUri = Uri.fromFile(fileUri)
                //通过FileProvider创建一个content类型的Uri
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    imageUri = FileProvider.getUriForFile(this@IdentityAuthenticationActivity, "com.lelandli.imagesynthesis.fileProvider", fileUri)
                }
                PhotoUtils.takePicture(this, this.imageUri!!, CODE_CAMERA_REQUEST)
            } else {
                showToastMsgLong("设备没有SD卡！")
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
        //调用系统相机申请拍照权限回调
            CAMERA_PERMISSIONS_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (hasSdcard()) {
                        imageUri = Uri.fromFile(fileUri)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                            imageUri = FileProvider.getUriForFile(this@IdentityAuthenticationActivity, "com.lelandli.imagesynthesis.fileProvider", fileUri)//通过FileProvider创建一个content类型的Uri
                        PhotoUtils.takePicture(this, this.imageUri!!, CODE_CAMERA_REQUEST)
                    } else {
                        showToastMsgLong("设备没有SD卡！")
                    }
                } else {
                    showToastMsgLong("请允许打开相机！！")
                }
            }
        //调用系统相册申请Sdcard权限回调
            STORAGE_PERMISSIONS_REQUEST_CODE -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                PhotoUtils.openPic(this, CODE_GALLERY_REQUEST)
            } else {
                showToastMsgLong("请允许打操作SDCard！！")
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
            //拍照完成回调
                CODE_CAMERA_REQUEST -> {
                    val newFile = CompressHelper.getDefault(this).compressToFile(fileUri)
                    val bitmap = PhotoUtils.getBitmapFromUri(Uri.fromFile(newFile), this)
                    if (bitmap != null) {
                        showImages(bitmap)
                    }
                }
            //访问相册完成回调
                CODE_GALLERY_REQUEST ->
                    if (hasSdcard()) {
                        var newUri = Uri.parse(PhotoUtils.getPath(this, data!!.data))
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            newUri = FileProvider.getUriForFile(this, "com.lelandli.imagesynthesis.fileProvider", File(newUri.path))
                        }
                        val bitmap = PhotoUtils.getBitmapFromUri(newUri!!, this)
                        if (bitmap != null) {
                            showImages(bitmap)
                        }
                    } else {
                        showToastMsgLong("设备没有SD卡！")
                    }
            }
        }
    }


    /**
     * 自动获取sdk权限
     */

    private fun autoObtainStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_PERMISSIONS_REQUEST_CODE)
        } else {
            PhotoUtils.openPic(this, CODE_GALLERY_REQUEST)
        }

    }

    private fun showImages(bitmap: Bitmap?) {
        if (firstBitmap == null){
            firstBitmap = bitmap
            id_caid_oneimage.setImageBitmap(bitmap)
            idcard_delete_image1.visibility = View.VISIBLE
            if (secondBitmap == null) {
                idcard_twoimage_layout.visibility = View.VISIBLE
                idcard_delete_image2.visibility = View.GONE
            }
        }else{
            secondBitmap = bitmap
            id_caid_twoimage.setImageBitmap(bitmap)
            idcard_delete_image2.visibility = View.VISIBLE
        }
    }

    /**
     * 检查设备是否存在SDCard的工具方法
     */
    private fun hasSdcard(): Boolean {
        val state = Environment.getExternalStorageState()
        return state == Environment.MEDIA_MOUNTED
    }

    /**
     * 弹出toast，显示时长为long
     * @param pMsg
     */
    @SuppressLint("ShowToast")
    private fun showToastMsgLong(pMsg: String) {
        if (toast == null) {
            toast = Toast.makeText(this,
                    pMsg,
                    Toast.LENGTH_LONG)
        } else {
            toast!!.setText(pMsg)
        }
        toast!!.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        deleteFile(fileUri.path)
        secondBitmap = null
        firstBitmap = null
    }

    override fun deleteFile(filePath: String): Boolean {
        val file = File(filePath)
        return file.isFile && file.exists() && file.delete()
    }
    private fun mergeBitmap(firstBitmap: Bitmap, secondBitmap: Bitmap): Bitmap {
        val width = firstBitmap.width +secondBitmap.width
        val height = firstBitmap.height
        val bitmap = Bitmap.createBitmap(width, height, firstBitmap.config)
        val canvas = Canvas(bitmap)
        canvas.drawBitmap(firstBitmap, 0F,0F, null)
        canvas.drawBitmap(secondBitmap, firstBitmap.width.toFloat(), 0F, null)
        var paint = Paint(Paint.ANTI_ALIAS_FLAG);
        paint.strokeWidth = 3F
        paint.textSize = 80F
        paint.color = Color.RED
        canvas.drawText(nameNumbers, 100F, 100F, paint)
        return bitmap
    }

}