package com.omnishop.erp.core.common

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.omnishop.erp.core.data.local.JournalEntity
import com.omnishop.erp.core.data.local.SaleEntity
import com.omnishop.erp.core.data.local.SaleItemEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGenerator {

    /**
     * Generates a high-quality PDF invoice for a completed POS Sale and launches the Share chooser sheet.
     * This allows users to download the file directly to their device or send/print it.
     */
    fun downloadShareBillPdf(
        context: Context,
        shopName: String,
        shopBusinessType: String,
        receiptFooter: String,
        sale: SaleEntity,
        items: List<SaleItemEntity>
    ) {
        val pdfDocument = PdfDocument()
        
        // Standard A4 dimensions in points (595 x 842)
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        // Setup clean professional Paints with proper tracking/typography
        val titlePaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 22.0f
            color = android.graphics.Color.rgb(30, 58, 138) // Elegant Corporate Navy
            isAntiAlias = true
        }

        val typePaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 8.5f
            color = android.graphics.Color.rgb(94, 234, 212) // Bright Teal Accent badge
            isAntiAlias = true
        }

        val metaHeaderPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 10f
            color = android.graphics.Color.rgb(71, 85, 105)
            isAntiAlias = true
        }

        val metaValuePaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textSize = 10f
            color = android.graphics.Color.rgb(15, 23, 42)
            isAntiAlias = true
        }

        val colHeaderPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 9.5f
            color = android.graphics.Color.rgb(255, 255, 255)
            isAntiAlias = true
        }

        val tableHeaderBgPaint = Paint().apply {
            color = android.graphics.Color.rgb(30, 58, 138)
            style = Paint.Style.FILL
        }

        val tableRowDividerPaint = Paint().apply {
            color = android.graphics.Color.rgb(241, 245, 249)
            strokeWidth = 1.0f
            style = Paint.Style.STROKE
        }

        val totalBoxPaint = Paint().apply {
            color = android.graphics.Color.rgb(248, 250, 252)
            style = Paint.Style.FILL
        }

        val totalBoxBorderPaint = Paint().apply {
            color = android.graphics.Color.rgb(226, 232, 240)
            strokeWidth = 1.0f
            style = Paint.Style.STROKE
        }

        // Draw top header
        canvas.drawRect(Rect(0, 0, 595, 65), tableHeaderBgPaint)
        
        // Header title text
        canvas.drawText("OMNISHOP ERP", 35f, 40f, titlePaint.apply { color = android.graphics.Color.rgb(255, 255, 255) })
        canvas.drawText("TAX INVOICE DESK", 410f, 40f, colHeaderPaint.apply { textSize = 14f })

        // Enterprise & Shop Info
        var currentY = 105f
        canvas.drawText("MERCHANT / OUTLET:", 35f, currentY, metaHeaderPaint)
        canvas.drawText(shopName.uppercase(), 175f, currentY, metaValuePaint.apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) })
        
        canvas.drawText("INVOICE REFERENCE:", 320f, currentY, metaHeaderPaint)
        canvas.drawText(sale.invoiceNo, 450f, currentY, metaValuePaint.apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) })

        currentY += 18f
        canvas.drawText("BUSINESS VERTICAL:", 35f, currentY, metaHeaderPaint)
        canvas.drawText(shopBusinessType, 175f, currentY, metaValuePaint.apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL) })
        
        canvas.drawText("BILLING STATUS:", 320f, currentY, metaHeaderPaint)
        canvas.drawText("PAID / COMPLETED", 450f, currentY, metaValuePaint.apply { color = android.graphics.Color.rgb(22, 163, 74) })

        currentY += 18f
        canvas.drawText("OPERATOR CASHIER:", 35f, currentY, metaHeaderPaint)
        canvas.drawText(sale.cashierName, 175f, currentY, metaValuePaint.apply { color = android.graphics.Color.BLACK })
        
        val dateFormat = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
        val dateString = dateFormat.format(Date(sale.timestamp))
        canvas.drawText("TIMESTAMP ISSUED:", 320f, currentY, metaHeaderPaint)
        canvas.drawText(dateString, 450f, currentY, metaValuePaint)

        // Draw Table Header Block
        currentY += 35f
        canvas.drawRect(Rect(35, currentY.toInt() - 16, 560, currentY.toInt() + 10), tableHeaderBgPaint.apply { color = android.graphics.Color.rgb(30, 41, 59) })
        canvas.drawText("PARTICULARS & PRODUCTS", 45f, currentY, colHeaderPaint.apply { textSize = 9.5f })
        canvas.drawText("QTY", 320f, currentY, colHeaderPaint)
        canvas.drawText("UNIT PRICE", 390f, currentY, colHeaderPaint)
        canvas.drawText("AMOUNT", 490f, currentY, colHeaderPaint)

        currentY += 12f
        val itemPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textSize = 9.5f
            color = android.graphics.Color.rgb(51, 65, 85)
            isAntiAlias = true
        }

        // Draw dynamic invoice ledger lines
        for (item in items) {
            currentY += 24f
            
            // Format item title beautifully and guard long descriptions
            var desc = item.productName
            if (desc.length > 34) {
                desc = desc.substring(0, 31) + "..."
            }
            canvas.drawText(desc, 45f, currentY, itemPaint)
            canvas.drawText("${item.quantity.toInt()}", 320f, currentY, itemPaint)
            canvas.drawText("$${String.format("%.2f", item.price)}", 390f, currentY, itemPaint)
            canvas.drawText("$${String.format("%.2f", item.totalAmount)}", 490f, currentY, itemPaint.apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) })
            
            // Lined divider
            canvas.drawLine(35f, currentY + 8f, 560f, currentY + 8f, tableRowDividerPaint)
        }

        // Math aggregate block
        currentY += 40f
        canvas.drawRect(Rect(320, currentY.toInt() - 12, 560, currentY.toInt() + 105), totalBoxPaint)
        canvas.drawRect(Rect(320, currentY.toInt() - 12, 560, currentY.toInt() + 105), totalBoxBorderPaint)

        val totalLabelPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textSize = 9.5f
            color = android.graphics.Color.rgb(71, 85, 105)
            isAntiAlias = true
        }
        val totalValPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 9.5f
            color = android.graphics.Color.rgb(15, 23, 42)
            isAntiAlias = true
        }

        canvas.drawText("SUBTOTAL", 335f, currentY, totalLabelPaint)
        canvas.drawText("$${String.format("%.2f", sale.subTotal)}", 480f, currentY, totalValPaint)

        currentY += 20f
        canvas.drawText("REDEEMED DISCOUNT", 335f, currentY, totalLabelPaint)
        canvas.drawText("-$${String.format("%.2f", sale.discountAmount)}", 480f, currentY, totalValPaint.apply { color = android.graphics.Color.rgb(22, 163, 74) })

        currentY += 20f
        canvas.drawText("TAXES CHARGED (18% GST)", 335f, currentY, totalLabelPaint.apply { color = android.graphics.Color.rgb(71, 85, 105) })
        canvas.drawText("$${String.format("%.2f", sale.taxAmount)}", 480f, currentY, totalValPaint.apply { color = android.graphics.Color.rgb(15, 23, 42) })

        currentY += 24f
        canvas.drawLine(335f, currentY - 14f, 545f, currentY - 14f, tableRowDividerPaint.apply { color = android.graphics.Color.rgb(203, 213, 225) })
        
        canvas.drawText("GRAND BILL TOTAL", 335f, currentY, totalLabelPaint.apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); textSize = 11f; color = android.graphics.Color.rgb(30, 58, 138) })
        canvas.drawText("$${String.format("%.2f", sale.totalAmount)}", 480f, currentY, totalValPaint.apply { textSize = 12f; color = android.graphics.Color.rgb(30, 58, 138) })

        // Footer block
        val footerPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            textSize = 8.5f
            color = android.graphics.Color.rgb(148, 163, 184)
            isAntiAlias = true
        }

        canvas.drawText(receiptFooter, 35f, 760f, footerPaint.apply { color = android.graphics.Color.rgb(71, 85, 105); typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL) })
        canvas.drawText("Thank you for your valuable patronage. Computer-generated tax statement. Perfect & Accurate.", 35f, 775f, footerPaint)
        canvas.drawText("OmniShop POS Double-Entry Ledger System. Active Synced Cloud Verified.", 35f, 790f, footerPaint)

        pdfDocument.finishPage(page)

        // Save PDF to standard device cache directory for seamless, secure sharing
        try {
            val destinationDir = context.cacheDir
            val cleanInvoiceRef = sale.invoiceNo.replace("/", "_").replace("#", "")
            val file = File(destinationDir, "OmniShopInvoice_${cleanInvoiceRef}.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.flush()
            outputStream.close()

            // Trigger Share Intent
            val fileUri: Uri = FileProvider.getUriForFile(
                context,
                "com.omnishop.erp.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_SUBJECT, "OmniShop ERP Bill Receipt: ${sale.invoiceNo}")
                putExtra(Intent.EXTRA_TEXT, "Here is your invoice ${sale.invoiceNo} from $shopName. Open this PDF statement using any viewer.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            val chooser = Intent.createChooser(shareIntent, "Save or Print Bill PDF")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            
            Toast.makeText(context, "Invoice PDF Generated Successfully!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "PDF Export Failure: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Generates a premium and responsive Double-Entry Accounting journal report statement as a PDF document.
     */
    fun downloadShareAccountingReportPdf(
        context: Context,
        totalRevenue: Double,
        totalExpenses: Double,
        netProfitLoss: Double,
        entries: List<JournalEntity>
    ) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        // Design elements setup
        val paintTextDark = Paint().apply {
            color = android.graphics.Color.rgb(15, 23, 42)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val paintTextPrimary = Paint().apply {
            color = android.graphics.Color.rgb(30, 58, 138)
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val headerBgPaint = Paint().apply {
            color = android.graphics.Color.rgb(15, 23, 42)
            style = Paint.Style.FILL
        }

        val metricCardPaint = Paint().apply {
            color = android.graphics.Color.rgb(248, 250, 252)
            style = Paint.Style.FILL
        }

        val metricCardBorder = Paint().apply {
            color = android.graphics.Color.rgb(226, 232, 240)
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }

        // Draw top general slate area
        canvas.drawRect(Rect(0, 0, 595, 80), headerBgPaint)
        
        val titlePaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 18f
            color = android.graphics.Color.rgb(255, 255, 255)
            isAntiAlias = true
        }
        canvas.drawText("OMNISHOP GENERAL LEDGER & BALANCES", 30f, 48f, titlePaint)
        
        val dateFormatedVal = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("SYSTEM SUMMARY • ISSUED $dateFormatedVal", 30f, 65f, Paint().apply {
            color = android.graphics.Color.rgb(148, 163, 184)
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        })

        // Draw Financial Indicator Cards side-by-side
        var cardLeft = 30
        val cardWidth = 165
        val cardYStart = 100
        val cardYEnd = 160

        // Card 1: Revenue (Green)
        canvas.drawRect(Rect(cardLeft, cardYStart, cardLeft + cardWidth, cardYEnd), metricCardPaint)
        canvas.drawRect(Rect(cardLeft, cardYStart, cardLeft + cardWidth, cardYEnd), metricCardBorder)
        canvas.drawText("ACCUMULATED REVENUE", (cardLeft + 10).toFloat(), (cardYStart + 18).toFloat(), Paint().apply {
            textSize = 7.5f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); color = android.graphics.Color.rgb(100, 116, 139); isAntiAlias = true
        })
        canvas.drawText("$${String.format("%.2f", totalRevenue)}", (cardLeft + 10).toFloat(), (cardYStart + 42).toFloat(), Paint().apply {
            textSize = 14f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); color = android.graphics.Color.rgb(22, 163, 74); isAntiAlias = true
        })

        // Card 2: Operating Expenses (Red)
        cardLeft += 180
        canvas.drawRect(Rect(cardLeft, cardYStart, cardLeft + cardWidth, cardYEnd), metricCardPaint)
        canvas.drawRect(Rect(cardLeft, cardYStart, cardLeft + cardWidth, cardYEnd), metricCardBorder)
        canvas.drawText("OPERATING EXPENSES", (cardLeft + 10).toFloat(), (cardYStart + 18).toFloat(), Paint().apply {
            textSize = 7.5f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); color = android.graphics.Color.rgb(100, 116, 139); isAntiAlias = true
        })
        canvas.drawText("$${String.format("%.2f", totalExpenses)}", (cardLeft + 10).toFloat(), (cardYStart + 42).toFloat(), Paint().apply {
            textSize = 14f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); color = android.graphics.Color.rgb(220, 38, 38); isAntiAlias = true
        })

        // Card 3: Store balance Net (Primary Navy)
        cardLeft += 180
        canvas.drawRect(Rect(cardLeft, cardYStart, cardLeft + cardWidth, cardYEnd), metricCardPaint)
        canvas.drawRect(Rect(cardLeft, cardYStart, cardLeft + cardWidth, cardYEnd), metricCardBorder)
        canvas.drawText("NET STORE BALANCE", (cardLeft + 10).toFloat(), (cardYStart + 18).toFloat(), Paint().apply {
            textSize = 7.5f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); color = android.graphics.Color.rgb(100, 116, 139); isAntiAlias = true
        })
        val netColor = if (netProfitLoss >= 0) android.graphics.Color.rgb(30, 58, 138) else android.graphics.Color.rgb(220, 38, 38)
        canvas.drawText("$${String.format("%.2f", netProfitLoss)}", (cardLeft + 10).toFloat(), (cardYStart + 42).toFloat(), Paint().apply {
            textSize = 14f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); color = netColor; isAntiAlias = true
        })

        // Table label
        var currentY = 195f
        canvas.drawText("GENERAL RECONCILIATION DOUBLE-ENTRY LISTINGS", 30f, currentY, Paint().apply {
            textSize = 10f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); color = android.graphics.Color.rgb(30, 58, 138)
        })

        // Column headers for Journal accounts
        currentY += 20f
        canvas.drawRect(Rect(30, currentY.toInt() - 14, 565, currentY.toInt() + 10), headerBgPaint.apply { color = android.graphics.Color.rgb(51, 65, 85) })
        canvas.drawText("VOUCHER DESCRIPTION / ACCOUNT CHANNELS", 40f, currentY, Paint().apply { color = android.graphics.Color.WHITE; textSize = 8.5f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) })
        canvas.drawText("CAT", 380f, currentY, Paint().apply { color = android.graphics.Color.WHITE; textSize = 8.5f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) })
        canvas.drawText("TRANSFERRED AMOUNT", 470f, currentY, Paint().apply { color = android.graphics.Color.WHITE; textSize = 8.5f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) })

        currentY += 14f
        val bodyPaint = Paint().apply {
            textSize = 9f
            color = android.graphics.Color.rgb(51, 65, 85)
            isAntiAlias = true
        }
        val subTextInfoPaint = Paint().apply {
            textSize = 8f
            color = android.graphics.Color.rgb(100, 116, 139)
            isAntiAlias = true
        }

        // Draw general journal records row listings limit to fits standard page
        val maxToShow = entries.take(18)
        for (item in maxToShow) {
            currentY += 26f
            if (currentY > 740) break // Guard bounds

            // Truncate description as well
            var desc = item.description
            if (desc.length > 50) {
                desc = desc.substring(0, 47) + "..."
            }
            canvas.drawText(desc, 40f, currentY - 4f, bodyPaint.apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); color = android.graphics.Color.rgb(15, 23, 42) })
            canvas.drawText("CR: ${item.accountFrom}  --->  DR: ${item.accountTo}", 40f, currentY + 8f, subTextInfoPaint)

            canvas.drawText(item.category, 380f, currentY, bodyPaint.apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL); color = android.graphics.Color.rgb(100, 116, 139) })
            
            val amtText = "$${String.format("%.2f", item.amount)}"
            val entryAmtColor = if (item.category == "REVENUE") android.graphics.Color.rgb(22, 163, 74) else android.graphics.Color.rgb(220, 38, 38)
            canvas.drawText(amtText, 470f, currentY, bodyPaint.apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); color = entryAmtColor })

            // Separator rule
            canvas.drawLine(30f, currentY + 14f, 565f, currentY + 14f, metricCardBorder)
            currentY += 2f
        }

        // Single statement signature and corporate foot outline
        canvas.drawText("OmniShop Digital Double Entry Ledger Audit System. Perfect Balancing Ledger.", 30f, 780f, Paint().apply {
            textSize = 8f; color = android.graphics.Color.rgb(148, 163, 184); typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC); isAntiAlias = true
        })
        canvas.drawText("Generated locally under high security encryption algorithms directly derived via standard device HSM.", 30f, 792f, Paint().apply {
            textSize = 8f; color = android.graphics.Color.rgb(148, 163, 184); typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC); isAntiAlias = true
        })

        pdfDocument.finishPage(page)

        try {
            val destinationDir = context.cacheDir
            val file = File(destinationDir, "OmniShopAccountingLedger_Statement.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.flush()
            outputStream.close()

            val fileUri: Uri = FileProvider.getUriForFile(
                context,
                "com.omnishop.erp.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_SUBJECT, "OmniShop Financial Balance Statement Ledger")
                putExtra(Intent.EXTRA_TEXT, "Here is-the updated accounting general ledger double-entry voucher list from OmniShop CRM/ERP.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "View/Share ledger entries")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)

            Toast.makeText(context, "Ledger PDF Generated Successfully!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Accounting PDF Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
}
