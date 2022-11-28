package com.example.practiceapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.*
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.practiceapp.databinding.ActivityEraseBinding
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


class EraseActivity : AppCompatActivity() {

    companion object {

        lateinit var imageFileName: File
        lateinit var context: Context
        public val TAG:String = "EraseActivity"
        private var highResolutionOutput: Bitmap? = null
        var msConn: MediaScannerConnection? = null

        lateinit var binding: ActivityEraseBinding
        private var initialDrawingCountLimit = 20
        private var offset = 250
        private var undoLimit = 10
        private var brushSize = 70.0f

        var MODE = 0
        private var isMultipleTouchErasing = false
        private var isTouchOnBitmap = false
        private var initialDrawingCount = 0
        private var updatedBrushSize = 0
        private var imageViewWidth = 0

        private var imageViewHeight = 0
        private var currentx = 0f
        private var currenty = 0f

        private var bitmapMaster: Bitmap? = null
        private lateinit var lastEditedBitmap: Bitmap
        private lateinit var originalBitmap: Bitmap
        private var resizedBitmap: Bitmap? = null


        var canvasMaster: Canvas? = null
        lateinit var paths: ArrayList<Path>

        lateinit var drawingPath: Path


        var redoPaths: ArrayList<Path>? = null
        var brushSizes: Vector<Int>? = null
        var redoBrushSizes: Vector<Int>? = null
    }

    private lateinit var mainViewSize: Point

    private var isImageResized = false


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEraseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        context = this
        paths = ArrayList<Path>()
        redoPaths = ArrayList<Path>()
        brushSizes = Vector<Int>()
        redoBrushSizes = Vector<Int>()
        MODE = 0
        drawingPath = Path()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        drawingPath = Path()
        val display = windowManager.defaultDisplay
        mainViewSize = Point()
        display.getSize(mainViewSize)

        initViews()

        fun makeHighResolutionOutput() {
            if (isImageResized) {
                var solidColor = Bitmap.createBitmap(
                    originalBitmap!!.width,
                    originalBitmap!!.height,
                    originalBitmap!!.config
                )
                val canvas = Canvas(solidColor!!)
                val paint = Paint()
                paint.color = Color.argb(255, 255, 255, 255)
                val src = Rect(0, 0, bitmapMaster!!.width, bitmapMaster!!.height)
                val dest = Rect(0, 0, originalBitmap!!.width, originalBitmap!!.height)
                canvas.drawRect(dest, paint)
                paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
                bitmapMaster.let { canvas.drawBitmap(bitmapMaster!!, src, dest, paint) }
                highResolutionOutput = null
                highResolutionOutput = Bitmap.createBitmap(
                    originalBitmap!!.width,
                    originalBitmap!!.height,
                    originalBitmap!!.config
                )
                val canvas1 = Canvas(highResolutionOutput!!)
                canvas1.drawBitmap(originalBitmap!!, 0.0f, 0.0f, null)
                val paint1 = Paint()
                paint1.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
                canvas1.drawBitmap(solidColor, 0.0f, 0.0f, paint1)
                if (solidColor != null && !solidColor.isRecycled) {
                    solidColor.recycle()
                    solidColor = null
                }
                return
            }
            highResolutionOutput = null
            highResolutionOutput = bitmapMaster!!.copy(bitmapMaster!!.config, true)
        }

//        originalBitmap = BitmapFactory.decodeResource(resources, R.drawable.img_1)

        val byteArray = intent.getByteArrayExtra("image")
        val bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray!!.size)

        originalBitmap=bmp
        Log.d(TAG, "onCreate: orignal bitmap : $originalBitmap")
        setBitMap()
        updateBrush((mainViewSize!!.x / 2).toFloat(), (mainViewSize!!.y / 2).toFloat())
    }

    fun updateBrush(x: Float, y: Float) {
        binding.brushContainingView.offset = offset.toFloat()
        binding.brushContainingView.centerx = x
        binding.brushContainingView.centery = y
        binding.brushContainingView.width = brushSize / 2.0f
        binding.brushContainingView.invalidate()
    }
    private fun setBitMap() {
        this.isImageResized = false;
        if (resizedBitmap != null) {
            resizedBitmap?.recycle();
            resizedBitmap = null;
        }
        if (bitmapMaster != null) {
            bitmapMaster!!.recycle();
            bitmapMaster = null;
        }
        canvasMaster = null;
        resizedBitmap = resizeBitmapByCanvas();

        lastEditedBitmap = resizedBitmap?.copy(Bitmap.Config.ARGB_8888, true)!!;
        bitmapMaster = Bitmap.createBitmap(lastEditedBitmap!!.getWidth(), lastEditedBitmap!!.getHeight(), Bitmap.Config.ARGB_8888);
        canvasMaster = Canvas(bitmapMaster!!);
        canvasMaster!!.drawBitmap(lastEditedBitmap!!, 0.0f, 0.0f, null);
        //set bitmap
        binding.drawingImageView.setImageBitmap(bitmapMaster);
        resetPathArrays();
        binding.drawingImageView.setPan(false);
        binding.brushContainingView!!.invalidate();
    }

    private fun resetPathArrays() {
        binding.ivUndo.isEnabled = false
        binding.ivRedo!!.isEnabled = false
        paths?.clear()
        brushSizes!!.clear()
        redoPaths!!.clear()
        redoBrushSizes!!.clear()
    }

    private fun resizeBitmapByCanvas(): Bitmap? {
        val width: Float
        val heigth: Float
        val orginalWidth = originalBitmap!!.width.toFloat()
        val orginalHeight = originalBitmap!!.height.toFloat()
        if (orginalWidth > orginalHeight) {
            width = imageViewWidth.toFloat()
            heigth = imageViewWidth.toFloat() * orginalHeight / orginalWidth
        } else {
            heigth = imageViewHeight.toFloat()
            width = imageViewHeight.toFloat() * orginalWidth / orginalHeight
        }
        if (width > orginalWidth || heigth > orginalHeight) {
            return originalBitmap
        }
        val background = Bitmap.createBitmap(width.toInt(), heigth.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(background)
        val scale = width / orginalWidth
        val yTranslation = (heigth - orginalHeight * scale) / 2.0f
        val transformation = Matrix()
        transformation.postTranslate(0.0f, yTranslation)
        transformation.preScale(scale, scale)
        val paint = Paint()
        paint.isFilterBitmap = true
        canvas.drawBitmap(originalBitmap!!, transformation, paint)
        isImageResized = true
        return background
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun initViews() {
        binding.ivUndo.setOnClickListener{
            undo()
        }

        binding.ivRedo.setOnClickListener {
            redo()
        }

        binding.ivDone.setOnClickListener {
            save()
        }



        binding.rlImageViewContainer.layoutParams.height =
            mainViewSize.y - (binding.llTopBar.layoutParams.height);
        imageViewWidth = mainViewSize.x
        imageViewHeight = binding.rlImageViewContainer.layoutParams.height;
        binding.drawingImageView.setOnTouchListener(OnTouchListner());
        binding.sbWidth.max = 150;
        binding.sbWidth.progress = (brushSize - 20.0f).toInt();
        binding.sbWidth.setOnSeekBarChangeListener(OnWidthSeekbarChangeListner());
        binding.sbOffset.max = 350;
        binding.sbOffset.progress = offset;
        binding.sbOffset.setOnSeekBarChangeListener(OnOffsetSeekbarChangeListner());


    }

    private fun save() {
        makeHighResolutionOutput()
        imageSaveByAsync().execute(*arrayOfNulls<String>(0))
    }

    private fun makeHighResolutionOutput() {
        if (isImageResized) {
            var solidColor = Bitmap.createBitmap(
                originalBitmap.width,
                originalBitmap.height,
                originalBitmap.config
            )
            val canvas = Canvas(solidColor!!)
            val paint = Paint()
            paint.color = Color.argb(255, 255, 255, 255)
            val src = bitmapMaster?.let { Rect(0, 0, bitmapMaster!!.width, it.height) }
            val dest = Rect(0, 0, originalBitmap.width, originalBitmap.height)
            canvas.drawRect(dest, paint)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
            bitmapMaster?.let { canvas.drawBitmap(it, src, dest, paint) }
            highResolutionOutput = null
            highResolutionOutput = Bitmap.createBitmap(
                originalBitmap.width,
                originalBitmap.height,
                originalBitmap.config
            )
            val b: Bitmap = (highResolutionOutput as Bitmap?)!!
            val canvas1 = Canvas(b)
            canvas1.drawBitmap(originalBitmap, 0.0f, 0.0f, null)
            val paint1 = Paint()
            paint1.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
            canvas1.drawBitmap(solidColor, 0.0f, 0.0f, paint1)
            if (solidColor != null && !solidColor.isRecycled) {
                solidColor.recycle()
                solidColor = null
            }
            return
        }
        highResolutionOutput = null
        highResolutionOutput = bitmapMaster!!.copy(bitmapMaster!!.config, true)
    }


    private class imageSaveByAsync() :
        AsyncTask<String?, Void?, Boolean>() {
        override fun onPreExecute() {
            //getWindow().setFlags(16, 16)
        }

        override fun doInBackground(vararg p0: String?): Boolean? {
            return try {
                savePhoto(highResolutionOutput)


                Log.d(TAG, "doInBackground: save ")
                java.lang.Boolean.valueOf(true)
            } catch (e: Exception) {
                Log.d(TAG, "doInBackground: not ")
                java.lang.Boolean.valueOf(false)
            }
        }

        override fun onPostExecute(success: Boolean) {
            Toast.makeText(context.applicationContext,"saved", Toast.LENGTH_SHORT).show()

        }


        fun savePhoto(bmp: Bitmap?) {

            var bmp = bmp
//          var imageFileName: File
            val out: FileOutputStream
            val imageFileFolder = File(Environment.getExternalStorageDirectory(), "ImageEraser")
            imageFileFolder.mkdir()
            val c = Calendar.getInstance()
            val date =
                c[Calendar.MONTH].toString() + c[Calendar.DAY_OF_MONTH].toString() + c[Calendar.YEAR].toString() + c[Calendar.HOUR_OF_DAY].toString() + c[Calendar.MINUTE].toString() + c[Calendar.SECOND].toString()
            val out2: FileOutputStream
            imageFileName = File(imageFileFolder, "$date.png")
            imageFileName = File(context.filesDir.toString() + File.separator + "$date.png")
            Log.d(TAG, "savePhoto: save = $imageFileName")
            try {

                out2 = FileOutputStream(imageFileName)
                bmp?.compress(Bitmap.CompressFormat.PNG, 70, out2)
                out = out2
                out.flush()
                out.close()
                Log.e(TAG, "savePhoto: $imageFileName.is")

            } catch (e: FileNotFoundException) {
                Log.e(TAG, "savePhoto: ${e.localizedMessage}")

                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            if (bmp != null && !bmp.isRecycled) {
                bmp.recycle()
                bmp = null

            }

            //scanPhoto(imageFileName.toString())
        }

        fun scanPhoto(imageFileName: String?) {
            msConn = MediaScannerConnection(
                context,
                ScanPhotoConnection(imageFileName.toString()));
            msConn!!.connect()
        }

        internal class ScanPhotoConnection(val `val$imageFileName`: String) :
            MediaScannerConnection.MediaScannerConnectionClient {
            override fun onMediaScannerConnected() {
                msConn!!.scanFile(`val$imageFileName`, null)
            }

            override fun onScanCompleted(path: String, uri: Uri) {
                msConn!!.disconnect()
            }
        }
    }


    fun undo() {
        var size: Int = paths.size
        if (size != 0) {
            if (size == 1) {
                binding.ivUndo.setEnabled(false)
            }
            size--
            redoPaths!!.add(paths.removeAt(size))
            redoBrushSizes!!.add(brushSizes!!.removeAt(size))
            if (!binding.ivRedo.isEnabled()) {
                binding.ivRedo.setEnabled(true)
            }
            UpdateCanvas()
        }
    }

    fun UpdateCanvas() {
        canvasMaster!!.drawColor(0, PorterDuff.Mode.CLEAR)
        canvasMaster!!.drawBitmap(lastEditedBitmap!!, 0.0f, 0.0f, null)
        var i = 0
        while (true) {
            if (i >= paths.size) {
                break
            }
            val brushSize = brushSizes!![i]
            val paint = Paint()
            paint.color = 0
            paint.style = Paint.Style.STROKE
            paint.isAntiAlias = true
            paint.strokeJoin = Paint.Join.ROUND
            paint.strokeCap = Paint.Cap.ROUND
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
            paint.strokeWidth = brushSize.toFloat()
            canvasMaster!!.drawPath(paths[i], paint)
            i += 1
        }
        binding.drawingImageView.invalidate()
    }

    fun redo() {
        var size = redoPaths!!.size
        if (size != 0) {
            if (size == 1) {
                binding.ivRedo.setEnabled(false)
            }
            size--
            paths.add(redoPaths!!.removeAt(size))
            brushSizes!!.add(redoBrushSizes!!.removeAt(size))
            if (!binding.ivUndo.isEnabled()) {
                binding.ivUndo.setEnabled(true)
            }
            UpdateCanvas()
        }
    }

    private class OnTouchListner internal constructor() : View.OnTouchListener {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onTouch(v: View, event: MotionEvent): Boolean {

            val action = event.action
            Log.d(TAG, "onTouch: action = $action / D = ${MotionEvent.ACTION_DOWN} / M = ${MotionEvent.ACTION_MOVE} / $MODE")
            if (!(event.pointerCount == 1 || isMultipleTouchErasing)) {
                if (initialDrawingCount > 0) {
                    UpdateCanvas()
                    drawingPath.reset()
                    initialDrawingCount = 0
                }
                Log.d(TAG, "onTouch: start ${event.pointerCount} / $isMultipleTouchErasing / $MODE")
                binding.drawingImageView.onTouchEvent(event)
                MODE = 2
            } else if (action == MotionEvent.ACTION_DOWN) {
                isTouchOnBitmap = false
                binding.drawingImageView.onTouchEvent(event)
                MODE = 1
                initialDrawingCount = 0
                isMultipleTouchErasing = false
                Log.d(TAG, "onTouch: ACTION_DOWN $event.x / $event.y")
                moveTopoint(event.x, event.y)
                Log.d(TAG, "onTouch: ACTION_DOWN updateBrush s ")
                updateBrush(event.x, event.y)
            } else if (action == MotionEvent.ACTION_MOVE) {
                if (MODE == 1) {
                    currentx = event.x
                    currenty = event.y
                    Log.d(TAG, "onTouch: ACTION_MOVE $event.x / $event.y")
                    updateBrush(currentx, currenty)
                    Log.d(TAG, "onTouch: ACTION_MOVE updateBrush ")
                    lineTopoint(bitmapMaster, currentx, currenty)
                    drawOnTouchMove()
                }
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
                if (MODE == 1) {
                    if (isTouchOnBitmap) {
                        addDrawingPathToArrayList()
                    }
                }
                isMultipleTouchErasing = false
                initialDrawingCount = 0
                MODE = 0
            }
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
                MODE = 0
            }
            return true
        }

        private fun addDrawingPathToArrayList() {
            if (paths!!.size >= undoLimit) {
                UpdateLastEiditedBitmapForUndoLimit()
                paths!!.removeAt(0)
                brushSizes!!.removeAt(0)
            }
            if (paths!!.size == 0) {
                binding.ivUndo.setEnabled(true)
                binding.ivRedo.setEnabled(false)
            }
            brushSizes!!.add(updatedBrushSize)
            paths!!.add(drawingPath!!)
            drawingPath = Path()
        }

        fun UpdateLastEiditedBitmapForUndoLimit() {
            val canvas = Canvas(lastEditedBitmap!!)
            var i = 0
            while (i < 1) {
                val brushSize = brushSizes!![i]
                val paint = Paint()
                paint.color = 0
                paint.style = Paint.Style.STROKE
                paint.isAntiAlias = true
                paint.strokeJoin = Paint.Join.ROUND
                paint.strokeCap = Paint.Cap.ROUND
                paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
                paint.strokeWidth = brushSize.toFloat()
                canvas.drawPath(paths!![i], paint)
                i += 1
            }
        }

        fun drawOnTouchMove() {
            Log.d(TAG, "drawOnTouchMove: start")
            val paint = Paint()
            paint.strokeWidth = updatedBrushSize.toFloat()
            paint.color = 0
            paint.style = Paint.Style.STROKE
            paint.isAntiAlias = true
            paint.strokeJoin = Paint.Join.ROUND
            paint.strokeCap = Paint.Cap.ROUND
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
            canvasMaster!!.drawPath(drawingPath!!, paint)
            binding.drawingImageView.invalidate()
        }

        fun lineTopoint(bm: Bitmap?, startx: Float, starty: Float) {
            Log.d(TAG, "lineTopoint: $startx / $starty")
            var starty = starty
            if (initialDrawingCount < initialDrawingCountLimit) {
                initialDrawingCount += 1
                if (initialDrawingCount == initialDrawingCountLimit) {
                    isMultipleTouchErasing = true
                }
            }
            val zoomScale = getImageViewZoom()
            starty -= offset.toFloat()
            val transLation = getImageViewTranslation()
            val projectedX =
                ((startx - transLation.x).toDouble() / zoomScale.toDouble()).toFloat().toInt()
            val projectedY =
                ((starty - transLation.y).toDouble() / zoomScale.toDouble()).toFloat().toInt()
            if (!isTouchOnBitmap && projectedX > 0 && projectedX < bm!!.width && projectedY > 0 && projectedY < bm!!.height) {
                isTouchOnBitmap = true
            }
            drawingPath!!.lineTo(projectedX.toFloat(), projectedY.toFloat())
        }

        fun updateBrush(x: Float, y: Float) {
            Log.d(TAG, "updateBrush: $x / $y")
            binding.brushContainingView.offset = offset.toFloat()
            binding.brushContainingView.centerx = x
            binding.brushContainingView.centery = y
            binding.brushContainingView.width = brushSize / 2.0f
            binding.brushContainingView.invalidate()
            Log.d(TAG, "updateBrush: finish")
        }

        fun moveTopoint(startx: Float, starty: Float) {
            Log.d(TAG, "moveTopoint: $startx / $starty")
            var starty = starty
            val zoomScale: Float = getImageViewZoom()
            starty -= offset.toFloat()
            if (redoPaths!!.size > 0) {
                resetRedoPathArrays()
            }
            val transLation: PointF = getImageViewTranslation()
            val projectedX =
                ((startx - transLation.x).toDouble() / zoomScale.toDouble()).toFloat().toInt()
            val projectedY =
                ((starty - transLation.y).toDouble() / zoomScale.toDouble()).toFloat().toInt()
            drawingPath.moveTo(projectedX.toFloat(), projectedY.toFloat())
            updatedBrushSize = (brushSize / zoomScale).toInt()
            Log.d(TAG, "moveTopoint: finish")
        }
        fun UpdateCanvas() {
            canvasMaster!!.drawColor(0, PorterDuff.Mode.CLEAR)
            canvasMaster!!.drawBitmap(lastEditedBitmap!!, 0.0f, 0.0f, null)
            var i = 0
            while (true) {
                if (i >= paths!!.size) {
                    break
                }
                val brushSize = brushSizes!![i]
                val paint = Paint()
                paint.color = 0
                paint.style = Paint.Style.STROKE
                paint.isAntiAlias = true
                paint.strokeJoin = Paint.Join.ROUND
                paint.strokeCap = Paint.Cap.ROUND
                paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
                paint.strokeWidth = brushSize.toFloat()
                canvasMaster!!.drawPath(paths!![i], paint)
                i += 1
            }
            binding.drawingImageView.invalidate()
        }


        fun getImageViewZoom(): Float {
            return binding.drawingImageView.getCurrentZoom()
        }

        fun resetRedoPathArrays() {
            binding.ivRedo.setEnabled(false)
            redoPaths!!.clear()
            redoBrushSizes!!.clear()
        }
        fun getImageViewTranslation(): PointF {
            return binding.drawingImageView.transForm
        }
    }

    private class OnWidthSeekbarChangeListner internal constructor() :
        SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            brushSize = progress.toFloat() + 20.0f
            updateBrushWidth()
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {}
        override fun onStopTrackingTouch(seekBar: SeekBar) {}
        fun updateBrushWidth() {
            binding.brushContainingView.width = brushSize / 2.0f
            binding.brushContainingView.invalidate()
        }
    }

    class OnOffsetSeekbarChangeListner : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
            offset = p1
            updateBrushOffset()
        }

        private fun updateBrushOffset() {
            val doffest = offset.toFloat() - binding.brushContainingView!!.offset
            binding.brushContainingView.centery += doffest
            binding.brushContainingView!!.offset = offset.toFloat()
            binding.brushContainingView!!.invalidate()
        }

        override fun onStartTrackingTouch(p0: SeekBar?) {

        }

        override fun onStopTrackingTouch(p0: SeekBar?) {

        }
    }
}