package com.adrencina.enchu.domain.use_case

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.Typeface
import android.os.Environment
import com.adrencina.enchu.domain.model.Obra
import com.adrencina.enchu.domain.model.Organization
import com.adrencina.enchu.domain.model.PresupuestoItem
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

    // --- CONFIGURACIÓN DE DISEÑO ---
    private val PAGE_WIDTH = 595
    private val PAGE_HEIGHT = 842
    private val MARGIN_X = 40f
    private val MARGIN_Y = 40f
    private val CONTENT_WIDTH = PAGE_WIDTH - (MARGIN_X * 2)
    
    // Colores
    private val COLOR_PRIMARY = Color.rgb(40, 40, 40)
    private val COLOR_ACCENT = Color.rgb(245, 245, 245)
    private val COLOR_LINE = Color.rgb(180, 180, 180)

    // Paints
    private val paintTitle = Paint()
    private val paintSubtitle = Paint()
    private val paintBody = Paint()
    private val paintBodyBold = Paint()
    private val paintHeaderTable = Paint()
    private val paintLine = Paint()

    suspend operator fun invoke(obra: Obra, items: List<PresupuestoItem>, organization: Organization?): File {
        return withContext(Dispatchers.IO) {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
            
            initPaints()

            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas
            var currentY = MARGIN_Y

            // 1. ENCABEZADO
            currentY = drawHeader(canvas, organization, obra)
            
            // 2. DATOS CLIENTE
            currentY = drawClientInfo(canvas, obra, currentY)

            currentY += 20f

            // 3. TABLA DE ÍTEMS
            currentY = drawTableHeader(canvas, currentY)

            val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "AR"))
            var totalMateriales = 0.0
            var totalManoObra = 0.0

            for (item in items) {
                if (currentY > PAGE_HEIGHT - 150f) {
                    pdfDocument.finishPage(page)
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    currentY = MARGIN_Y + 20f
                    currentY = drawTableHeader(canvas, currentY)
                }

                val totalItem = item.cantidad * item.precioUnitario
                
                // Acumular según tipo
                if (item.tipo == "MANO_DE_OBRA") {
                    totalManoObra += totalItem
                } else {
                    totalMateriales += totalItem
                }

                val codigo = item.tipo.take(3).uppercase() + "-" + item.id.take(4).uppercase()
                
                canvas.drawText(codigo, MARGIN_X + 5, currentY, paintBody)
                val desc = if (item.descripcion.length > 50) item.descripcion.take(47) + "..." else item.descripcion
                canvas.drawText(desc, MARGIN_X + 60, currentY, paintBody)
                
                // Columnas ajustadas
                drawRightText(canvas, if(item.cantidad % 1.0 == 0.0) item.cantidad.toInt().toString() else String.format("%.1f", item.cantidad), MARGIN_X + 340, currentY, paintBody)
                drawRightText(canvas, currencyFormat.format(item.precioUnitario), MARGIN_X + 420, currentY, paintBody)
                drawRightText(canvas, currencyFormat.format(totalItem), MARGIN_X + CONTENT_WIDTH - 5, currentY, paintBody)

                currentY += 20f
                paintLine.color = Color.rgb(240, 240, 240)
                canvas.drawLine(MARGIN_X, currentY - 15, MARGIN_X + CONTENT_WIDTH, currentY - 15, paintLine)
                paintLine.color = COLOR_LINE
            }

            // 4. TOTALES
            if (currentY > PAGE_HEIGHT - 120f) {
                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                currentY = MARGIN_Y + 20f
            }

            currentY += 10f
            val boxTotalWidth = 200f
            val boxTotalX = MARGIN_X + CONTENT_WIDTH - boxTotalWidth
            
            // Cálculos con Descuento
            val subtotal = totalMateriales + totalManoObra
            val descuentoMonto = subtotal * (obra.descuento / 100)
            val totalFinal = subtotal - descuentoMonto

            // Desglose por tipo
            canvas.drawText("Total Materiales:", boxTotalX + 10, currentY + 15, paintBody)
            drawRightText(canvas, currencyFormat.format(totalMateriales), MARGIN_X + CONTENT_WIDTH - 5, currentY + 15, paintBody)
            currentY += 20f
            
            canvas.drawText("Total Mano de Obra:", boxTotalX + 10, currentY + 15, paintBody)
            drawRightText(canvas, currencyFormat.format(totalManoObra), MARGIN_X + CONTENT_WIDTH - 5, currentY + 15, paintBody)
            currentY += 25f
            
            // --- INSERCIÓN: DESCUENTO ---
            if (descuentoMonto > 0) {
                canvas.drawText("Descuento (${obra.descuento.toInt()}%):", boxTotalX + 10, currentY + 15, paintBody)
                drawRightText(canvas, "- ${currencyFormat.format(descuentoMonto)}", MARGIN_X + CONTENT_WIDTH - 5, currentY + 15, paintBody)
                currentY += 25f
            }

            // Total Final
            canvas.drawRect(boxTotalX, currentY, MARGIN_X + CONTENT_WIDTH, currentY + 30, paintLine)
            paintHeaderTable.textSize = 12f
            canvas.drawText("TOTAL", boxTotalX + 10, currentY + 20, paintHeaderTable)
            drawRightText(canvas, currencyFormat.format(totalFinal), MARGIN_X + CONTENT_WIDTH - 5, currentY + 20, paintHeaderTable)
            
            // --- INSERCIÓN: NOTAS ---
            currentY += 50f
            if (obra.notas.isNotBlank()) {
                canvas.drawText("Notas:", MARGIN_X, currentY, paintBodyBold)
                currentY += 15f
                
                val words = obra.notas.split(" ")
                var line = ""
                for (word in words) {
                    if (paintBody.measureText(line + word) < CONTENT_WIDTH) {
                        line += "$word "
                    } else {
                        canvas.drawText(line, MARGIN_X, currentY, paintBody)
                        currentY += 12f
                        line = "$word "
                    }
                }
                canvas.drawText(line, MARGIN_X, currentY, paintBody)
            }
            
            val footerY = PAGE_HEIGHT - MARGIN_Y
            
            // Texto legal más visible
            val paintLegal = Paint(paintBody).apply { 
                textSize = 11f 
                letterSpacing = 0.08f 
                isAntiAlias = true
            }
            
            val diasValidez = if (obra.validez > 0) obra.validez else 15
            canvas.drawText("Presupuesto válido por $diasValidez días. Documento no válido como factura.", MARGIN_X, footerY, paintLegal)
            drawRightText(canvas, "Página 1", MARGIN_X + CONTENT_WIDTH, footerY, paintBody)

            pdfDocument.finishPage(page)

            val fileName = "Presupuesto_${obra.nombreObra.replace(" ", "_")}.pdf"
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

    private fun initPaints() {
        paintTitle.apply {
            color = COLOR_PRIMARY
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            letterSpacing = 0.05f
        }
        paintSubtitle.apply {
            color = COLOR_PRIMARY
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            letterSpacing = 0.05f
        }
        paintBody.apply {
            color = Color.DKGRAY
            textSize = 10f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
            letterSpacing = 0.02f
        }
        paintBodyBold.apply {
            color = Color.BLACK
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            letterSpacing = 0.02f
        }
        paintHeaderTable.apply {
            color = Color.BLACK
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            letterSpacing = 0.02f
        }
        paintLine.apply {
            color = COLOR_LINE
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }
    }

    private fun drawHeader(canvas: Canvas, organization: Organization?, obra: Obra): Float {
        var headerY = MARGIN_Y
        if (organization != null) {
            if (organization.logoUrl.isNotBlank()) {
                try {
                    val url = URL(organization.logoUrl)
                    val bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                    val targetHeight = 60
                    val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
                    val targetWidth = (targetHeight * ratio).toInt()
                    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
                    canvas.drawBitmap(scaledBitmap, MARGIN_X, headerY, null)
                } catch (e: Exception) {
                    // Fallback
                }
            }
            val textX = MARGIN_X + 85f
            canvas.drawText(organization.name.uppercase(), textX, headerY + 20, paintTitle)
            canvas.drawText(organization.businessAddress, textX, headerY + 35, paintBody)
            canvas.drawText("${organization.businessPhone} | ${organization.businessEmail}", textX, headerY + 48, paintBody)
            
            // INSERCION: DATOS FISCALES DEL USUARIO (SI EXISTEN)
            if (organization.cuit.isNotBlank()) {
                 canvas.drawText("CUIT: ${organization.cuit} - ${organization.taxCondition}", textX, headerY + 60, paintBody)
            }
        }

        val boxWidth = 180f
        val boxHeight = 70f
        val boxX = MARGIN_X + CONTENT_WIDTH - boxWidth
        canvas.drawRect(boxX, headerY, boxX + boxWidth, headerY + boxHeight, paintLine)
        
        paintSubtitle.textAlign = Paint.Align.CENTER
        canvas.drawText("PRESUPUESTO", boxX + (boxWidth / 2), headerY + 25, paintSubtitle)
        paintSubtitle.textAlign = Paint.Align.LEFT
        
        canvas.drawLine(boxX, headerY + 35, boxX + boxWidth, headerY + 35, paintLine)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        canvas.drawText("Fecha: ${dateFormat.format(Date())}", boxX + 10, headerY + 50, paintBody)
        
        val budgetNumStr = if (obra.budgetNumber > 0) {
            String.format("%08d", obra.budgetNumber)
        } else {
            "PENDIENTE"
        }
        canvas.drawText("N°: 0001-$budgetNumStr", boxX + 10, headerY + 65, paintBody)

        return headerY + 90f
    }

    private fun drawClientInfo(canvas: Canvas, obra: Obra, startY: Float): Float {
        val boxHeight = 50f
        canvas.drawRect(MARGIN_X, startY, MARGIN_X + CONTENT_WIDTH, startY + boxHeight, paintLine)
        val paintLabel = Paint(paintBodyBold).apply { textSize = 8f; color = Color.GRAY }
        canvas.drawText("DATOS DEL CLIENTE", MARGIN_X + 5, startY - 5, paintLabel)

        val cuitDisplay = if (obra.clienteCuit.isNotBlank()) obra.clienteCuit else "-"
        val ivaDisplay = if (obra.clientTaxCondition.isNotBlank()) obra.clientTaxCondition else "Consumidor Final"

        canvas.drawText("Cliente: ${obra.clienteNombre}", MARGIN_X + 10, startY + 20, paintBodyBold)
        canvas.drawText("Dirección: ${obra.direccion}", MARGIN_X + 10, startY + 40, paintBody)
        canvas.drawText("Teléfono: ${obra.telefono}", MARGIN_X + CONTENT_WIDTH / 2 + 10, startY + 20, paintBody)
        canvas.drawText("IVA: $ivaDisplay  |  CUIT: $cuitDisplay", MARGIN_X + CONTENT_WIDTH / 2 + 10, startY + 40, paintBody)

        return startY + boxHeight
    }

    private fun drawTableHeader(canvas: Canvas, startY: Float): Float {
        val headerHeight = 25f
        val paintFill = Paint().apply { color = COLOR_ACCENT }
        canvas.drawRect(MARGIN_X, startY, MARGIN_X + CONTENT_WIDTH, startY + headerHeight, paintFill)
        canvas.drawRect(MARGIN_X, startY, MARGIN_X + CONTENT_WIDTH, startY + headerHeight, paintLine)
        
        val textY = startY + 17
        canvas.drawText("CÓD.", MARGIN_X + 5, textY, paintHeaderTable)
        canvas.drawText("DESCRIPCIÓN", MARGIN_X + 60, textY, paintHeaderTable)
        drawRightText(canvas, "CANT.", MARGIN_X + 340, textY, paintHeaderTable)
        drawRightText(canvas, "P. UNIT", MARGIN_X + 420, textY, paintHeaderTable)
        drawRightText(canvas, "TOTAL", MARGIN_X + CONTENT_WIDTH - 5, textY, paintHeaderTable)

        return startY + headerHeight + 15f
    }

    private fun drawRightText(canvas: Canvas, text: String, x: Float, y: Float, paint: Paint) {
        val originalAlign = paint.textAlign
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(text, x, y, paint)
        paint.textAlign = originalAlign
    }
}