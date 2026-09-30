package com.example.data.ai

import android.util.Log
import com.example.BuildConfig
import com.example.domain.model.CurrencyUtils
import com.example.domain.model.MonthSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiFinancialService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun generateFinancialAdvice(
        userPrompt: String,
        monthSummary: MonthSummary,
        topCategories: List<Pair<String, Double>>,
        pendingBillsCount: Int
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        // Context summary for AI
        val contextPrompt = buildString {
            appendLine("Você é o Assistente Financeiro Inteligente do aplicativo nativo Min Finanças.")
            appendLine("Responda sempre em Português do Brasil (PT-BR) de forma amigável, direta, minimalista e altamente prática.")
            appendLine("Dados financeiros do usuário no mês atual (${monthSummary.monthNamePtBr}):")
            appendLine("- Saldo Atual: ${CurrencyUtils.formatBrl(monthSummary.currentBalance)}")
            appendLine("- Receitas no mês: ${CurrencyUtils.formatBrl(monthSummary.totalIncome)}")
            appendLine("- Despesas pagas: ${CurrencyUtils.formatBrl(monthSummary.paidExpenses)}")
            appendLine("- Despesas previstas (a pagar): ${CurrencyUtils.formatBrl(monthSummary.pendingExpenses)} ($pendingBillsCount contas)")
            appendLine("- Saldo Projetado para o fim do mês: ${CurrencyUtils.formatBrl(monthSummary.projectedBalance)}")
            appendLine("- Taxa de poupança atual: ${String.format("%.1f", monthSummary.savingsRate)}%")
            if (topCategories.isNotEmpty()) {
                appendLine("- Maiores categorias de despesa:")
                topCategories.take(3).forEach { (cat, amt) ->
                    appendLine("  * $cat: ${CurrencyUtils.formatBrl(amt)}")
                }
            }
            appendLine()
            appendLine("Pergunta/Solicitação do usuário: \"$userPrompt\"")
        }

        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // High-quality local financial intelligence heuristic fallback
            return@withContext generateLocalFinancialDiagnosis(userPrompt, monthSummary, topCategories, pendingBillsCount)
        }

        try {
            val jsonBody = JSONObject().apply {
                val contents = JSONArray()
                val contentObj = JSONObject().apply {
                    val parts = JSONArray()
                    parts.put(JSONObject().put("text", contextPrompt))
                    put("parts", parts)
                }
                contents.put(contentObj)
                put("contents", contents)

                val genConfig = JSONObject().apply {
                    put("temperature", 0.7)
                    put("maxOutputTokens", 800)
                }
                put("generationConfig", genConfig)
            }

            // Using gemini-2.5-flash as specified in requirements
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val respStr = response.body?.string() ?: ""
                    val respJson = JSONObject(respStr)
                    val candidates = respJson.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val candidate = candidates.getJSONObject(0)
                        val content = candidate.optJSONObject("content")
                        val parts = content?.optJSONArray("parts")
                        val text = parts?.optJSONObject(0)?.optString("text")
                        if (!text.isNullOrBlank()) {
                            return@withContext text.trim()
                        }
                    }
                }
            }
            // Fallback if API returned empty, quota exhausted or error
            generateLocalFinancialDiagnosis(userPrompt, monthSummary, topCategories, pendingBillsCount)
        } catch (e: Exception) {
            Log.e("GeminiService", "API error: ${e.message}")
            generateLocalFinancialDiagnosis(userPrompt, monthSummary, topCategories, pendingBillsCount)
        }
    }

    private fun generateLocalFinancialDiagnosis(
        prompt: String,
        summary: MonthSummary,
        topCategories: List<Pair<String, Double>>,
        pendingBills: Int
    ): String {
        val lower = prompt.lowercase()
        val savingsRate = summary.savingsRate
        val topCatName = topCategories.firstOrNull()?.first ?: "Alimentação e Moradia"
        val topCatVal = topCategories.firstOrNull()?.second ?: summary.paidExpenses

        return when {
            lower.contains("diagnóstico") || lower.contains("analis") || lower.contains("resumo") -> {
                buildString {
                    append("📊 **Diagnóstico Financeiro de ${summary.monthNamePtBr}:**\n\n")
                    if (savingsRate >= 20.0) {
                        append("🟢 **Excelente ritmo financeiro!** Sua taxa de poupança está em **${String.format("%.1f", savingsRate)}%**, superando a regra recomendada dos 50/30/20.\n\n")
                    } else if (savingsRate >= 0) {
                        append("🟡 **Atenção moderada:** Sua taxa de poupança está em **${String.format("%.1f", savingsRate)}%**. O ideal é destinar pelo menos 15% a 20% das receitas para caixinhas de reserva.\n\n")
                    } else {
                        append("🔴 **Alerta de déficit:** Seus gastos totais projetados superam as receitas do mês em ${CurrencyUtils.formatBrl(kotlin.math.abs(summary.projectedBalance))}.\n\n")
                    }
                    if (topCategories.isNotEmpty()) {
                        append("📌 **Maior ralo de dinheiro:** A categoria **$topCatName** consumiu ${CurrencyUtils.formatBrl(topCatVal)}.\n")
                    }
                    if (pendingBills > 0) {
                        append("⏰ **Contas Pendentes:** Você tem $pendingBills conta(s) a pagar somando ${CurrencyUtils.formatBrl(summary.pendingExpenses)}. Saldo projetado após quitação: ${CurrencyUtils.formatBrl(summary.projectedBalance)}.")
                    } else {
                        append("✅ Todas as contas cadastradas do mês já estão quitadas!")
                    }
                }
            }
            lower.contains("economizar") || lower.contains("cortar") || lower.contains("poupar") -> {
                buildString {
                    append("💡 **Estratégias de Economia Personalizadas:**\n\n")
                    append("1. **Revisão de $topCatName:** Como representa o maior volume dos seus desembolsos (${CurrencyUtils.formatBrl(topCatVal)}), uma redução de apenas 10% aqui libera ${CurrencyUtils.formatBrl(topCatVal * 0.1)} a mais por mês.\n")
                    append("2. **Regra das 48 Horas:** Antes de compras não essenciais na categoria Lazer ou Compras, aguarde 2 dias para avaliar a real necessidade.\n")
                    append("3. **Automatize para as Caixinhas:** Guarde seu aporte para a Reserva de Emergência assim que o salário cair, e não o que sobrar no fim do mês.")
                }
            }
            lower.contains("meta") || lower.contains("caixinha") || lower.contains("viagem") -> {
                buildString {
                    append("🎯 **Recomendação para Caixinhas & Metas:**\n\n")
                    append("Seu saldo livre projetado é de **${CurrencyUtils.formatBrl(summary.projectedBalance)}**.\n")
                    append("Se você destinar 50% dessa sobra (${CurrencyUtils.formatBrl(summary.projectedBalance * 0.5)}) mensalmente para sua meta prioritária, acelerará a conclusão em até 3 meses antes do prazo!")
                }
            }
            else -> {
                buildString {
                    append("Olá! Com base nos seus números de **${summary.monthNamePtBr}**:\n")
                    append("• Saldo em conta: **${CurrencyUtils.formatBrl(summary.currentBalance)}**\n")
                    append("• Entradas: **${CurrencyUtils.formatBrl(summary.totalIncome)}** | Gastos Pagos: **${CurrencyUtils.formatBrl(summary.paidExpenses)}**\n")
                    append("• Taxa de economia: **${String.format("%.1f", savingsRate)}%**\n\n")
                    append("Dica: Você pode me pedir um *diagnóstico completo*, *dicas de corte de gastos* ou *análise das suas caixinhas* a qualquer momento!")
                }
            }
        }
    }
}
