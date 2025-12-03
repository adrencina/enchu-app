package com.adrencina.enchu.domain.use_case

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.adrencina.enchu.data.model.Organization
import com.adrencina.enchu.data.model.Obra
import com.adrencina.enchu.data.model.PresupuestoItem
import android.graphics.BitmapFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class GeneratePresupuestoPdfUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend operator fun invoke(obra: Obra, items: List<PresupuestoItem>, organization: Organization?): File {
        return withContext(Dispatchers.IO) {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas
            val paint = Paint()
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "AR"))
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            // Margin
            val margin = 40f
            var yPosition = margin

            // --- BRANDING HEADER ---
            if (organization != null) {
                // Logo
                if (organization.logoUrl.isNotBlank()) {
                    try {
                        val url = URL(organization.logoUrl)
                        val bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                        val scaledBitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, 80, 80, true)
                        canvas.drawBitmap(scaledBitmap, margin, yPosition, paint)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                
                // Business Info (Right Aligned)
                paint.textSize = 16f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.color = Color.BLACK
                paint.textAlign = Paint.Align.RIGHT
                
                val endX = 555f
                canvas.drawText(organization.name.ifBlank { "Presupuesto" }, endX, yPosition + 20, paint)
                
                paint.textSize = 10f
                paint.typeface = Typeface.DEFAULT
                paint.color = Color.DKGRAY
                
                var contactY = yPosition + 35
                if (organization.businessPhone.isNotBlank()) {
                    canvas.drawText(organization.businessPhone, endX, contactY, paint)
                    contactY += 12
                }
                if (organization.businessEmail.isNotBlank()) {
                    canvas.drawText(organization.businessEmail, endX, contactY, paint)
                    contactY += 12
                }
                if (organization.businessAddress.isNotBlank()) {
                    canvas.drawText(organization.businessAddress, endX, contactY, paint)
                }
                
                paint.textAlign = Paint.Align.LEFT // Reset
                yPosition += 100 // Space after header
            } else {
                // Default Title if no org
                paint.textSize = 24f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.color = Color.BLACK
                canvas.drawText("Presupuesto de Obra", margin, yPosition + 24, paint)
                yPosition += 60
            }
            
            // ... Rest of the PDF logic (Obra Info, Table, Totals) ...
            // Obra Info
            paint.textSize = 14f
            paint.typeface = Typeface.DEFAULT
            paint.color = Color.BLACK
            canvas.drawText("Obra: ${obra.nombreObra}", margin, yPosition, paint)
            yPosition += 20
            canvas.drawText("Cliente: ${obra.clienteNombre}", margin, yPosition, paint)
            yPosition += 20
            canvas.drawText("Fecha: ${dateFormat.format(Date())}", margin, yPosition, paint)
            yPosition += 40

            // Table Header
            paint.style = Paint.Style.FILL
            paint.color = Color.LTGRAY
            canvas.drawRect(margin, yPosition - 15, 555f, yPosition + 10, paint)
            
            paint.color = Color.BLACK
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Descripción", margin + 5, yPosition, paint)
            canvas.drawText("Cant.", 350f, yPosition, paint)
            canvas.drawText("Precio Unit.", 420f, yPosition, paint)
            canvas.drawText("Total", 500f, yPosition, paint)
            yPosition += 30

            // Items
            paint.typeface = Typeface.DEFAULT
            paint.textSize = 12f
            
            var totalMateriales = 0.0
            var totalManoObra = 0.0

            for (item in items) {
                val itemTotal = item.cantidad * item.precioUnitario
                if (item.tipo == "MATERIAL") totalMateriales += itemTotal else totalManoObra += itemTotal
                
                val desc = if (item.descripcion.length > 45) item.descripcion.take(42) + "..." else item.descripcion
                
                canvas.drawText(desc, margin + 5, yPosition, paint)
                canvas.drawText(item.cantidad.toString(), 350f, yPosition, paint)
                canvas.drawText(currencyFormat.format(item.precioUnitario), 420f, yPosition, paint)
                canvas.drawText(currencyFormat.format(itemTotal), 500f, yPosition, paint)
                
                yPosition += 20
                
                if (yPosition > 800) break 
            }

            yPosition += 20
            canvas.drawLine(margin, yPosition, 555f, yPosition, paint)
            yPosition += 30

            // Totals
            paint.textSize = 14f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            
            canvas.drawText("Total Materiales:", 350f, yPosition, paint)
            canvas.drawText(currencyFormat.format(totalMateriales), 500f, yPosition, paint)
            yPosition += 20
            
            canvas.drawText("Total Mano de Obra:", 350f, yPosition, paint)
            canvas.drawText(currencyFormat.format(totalManoObra), 500f, yPosition, paint)
            yPosition += 30
            
            paint.textSize = 16f
            canvas.drawText("TOTAL GENERAL:", 300f, yPosition, paint)
            canvas.drawText(currencyFormat.format(totalMateriales + totalManoObra), 500f, yPosition, paint)

            pdfDocument.finishPage(page)

            // Save file
            val fileName = "Presupuesto_${obra.nombreObra.replace(" ", "_")}_${System.currentTimeMillis()}.pdf"
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
            
            try {
                pdfDocument.writeTo(FileOutputStream(file))
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pdfDocument.close()
            }

            file
        }
    }
}
