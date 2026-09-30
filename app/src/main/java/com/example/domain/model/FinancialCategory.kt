package com.example.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class TransactionType(val labelPtBr: String) {
    INCOME("Receita"),
    EXPENSE("Despesa")
}

data class CategoryDef(
    val id: String,
    val namePtBr: String,
    val icon: ImageVector,
    val defaultType: TransactionType,
    val color: Color
)

object FinancialCategories {
    val ALL: List<CategoryDef> = listOf(
        // Despesas
        CategoryDef("Alimentação", "Alimentação", Icons.Default.Restaurant, TransactionType.EXPENSE, Color(0xFFF59E0B)),
        CategoryDef("Moradia", "Moradia", Icons.Default.Home, TransactionType.EXPENSE, Color(0xFF6366F1)),
        CategoryDef("Transporte", "Transporte", Icons.Default.DirectionsCar, TransactionType.EXPENSE, Color(0xFF0EA5E9)),
        CategoryDef("Lazer", "Lazer", Icons.Default.SportsEsports, TransactionType.EXPENSE, Color(0xFFEC4899)),
        CategoryDef("Saúde", "Saúde", Icons.Default.LocalHospital, TransactionType.EXPENSE, Color(0xFFEF4444)),
        CategoryDef("Educação", "Educação", Icons.Default.School, TransactionType.EXPENSE, Color(0xFF8B5CF6)),
        CategoryDef("Compras", "Compras", Icons.Default.ShoppingCart, TransactionType.EXPENSE, Color(0xFFF97316)),
        CategoryDef("Serviços", "Serviços", Icons.Default.Bolt, TransactionType.EXPENSE, Color(0xFF14B8A6)),

        // Receitas
        CategoryDef("Salário", "Salário", Icons.Default.Payments, TransactionType.INCOME, Color(0xFF10B981)),
        CategoryDef("Férias", "Férias", Icons.Default.Flight, TransactionType.INCOME, Color(0xFF06B6D4)),
        CategoryDef("13º Salário", "13º Salário", Icons.Default.CardGiftcard, TransactionType.INCOME, Color(0xFF84CC16)),
        CategoryDef("Investimentos", "Investimentos", Icons.Default.TrendingUp, TransactionType.INCOME, Color(0xFF3B82F6)),
        CategoryDef("Freelance", "Freelance", Icons.Default.Work, TransactionType.INCOME, Color(0xFFA855F7)),

        // Outros
        CategoryDef("Outros", "Outros", Icons.Default.Category, TransactionType.EXPENSE, Color(0xFF64748B))
    )

    fun getByName(name: String): CategoryDef {
        return ALL.firstOrNull { it.namePtBr.equals(name, ignoreCase = true) }
            ?: CategoryDef(name, name, Icons.Default.Category, TransactionType.EXPENSE, Color(0xFF64748B))
    }

    val expenseCategories: List<CategoryDef> = ALL.filter { it.defaultType == TransactionType.EXPENSE || it.namePtBr == "Outros" }
    val incomeCategories: List<CategoryDef> = ALL.filter { it.defaultType == TransactionType.INCOME || it.namePtBr == "Outros" }
}
