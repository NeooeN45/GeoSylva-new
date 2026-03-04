package com.forestry.counter.domain.model

import kotlinx.serialization.Serializable

enum class IbpCriterionId(val code: String, val group: IbpGroup) {
    E1("E1", IbpGroup.A),
    E2("E2", IbpGroup.A),
    GB("GB", IbpGroup.A),
    BMS("BMS", IbpGroup.A),
    BMC("BMC", IbpGroup.A),
    DMH("DMH", IbpGroup.A),
    VS("VS", IbpGroup.A),
    CF("CF", IbpGroup.B),
    CO("CO", IbpGroup.B),
    HC("HC", IbpGroup.B);

    companion object {
        val ALL = values().toList()
        val GROUP_A = values().filter { it.group == IbpGroup.A }
        val GROUP_B = values().filter { it.group == IbpGroup.B }
    }
}

enum class IbpGroup { A, B }

@Serializable
data class IbpAnswers(
    val e1: Int = -1,
    val e2: Int = -1,
    val gb: Int = -1,
    val bms: Int = -1,
    val bmc: Int = -1,
    val dmh: Int = -1,
    val vs: Int = -1,
    val cf: Int = -1,
    val co: Int = -1,
    val hc: Int = -1
) {
    fun get(id: IbpCriterionId): Int = when (id) {
        IbpCriterionId.E1  -> e1
        IbpCriterionId.E2  -> e2
        IbpCriterionId.GB  -> gb
        IbpCriterionId.BMS -> bms
        IbpCriterionId.BMC -> bmc
        IbpCriterionId.DMH -> dmh
        IbpCriterionId.VS  -> vs
        IbpCriterionId.CF  -> cf
        IbpCriterionId.CO  -> co
        IbpCriterionId.HC  -> hc
    }

    fun set(id: IbpCriterionId, value: Int): IbpAnswers = when (id) {
        IbpCriterionId.E1  -> copy(e1 = value)
        IbpCriterionId.E2  -> copy(e2 = value)
        IbpCriterionId.GB  -> copy(gb = value)
        IbpCriterionId.BMS -> copy(bms = value)
        IbpCriterionId.BMC -> copy(bmc = value)
        IbpCriterionId.DMH -> copy(dmh = value)
        IbpCriterionId.VS  -> copy(vs = value)
        IbpCriterionId.CF  -> copy(cf = value)
        IbpCriterionId.CO  -> copy(co = value)
        IbpCriterionId.HC  -> copy(hc = value)
    }

    val scoreA: Int get() {
        val vals = listOf(e1, e2, gb, bms, bmc, dmh, vs)
        if (vals.any { it < 0 }) return -1
        return vals.sum()
    }

    val scoreB: Int get() {
        val vals = listOf(cf, co, hc)
        if (vals.any { it < 0 }) return -1
        return vals.sum()
    }

    val scoreTotal: Int get() {
        val a = scoreA
        val b = scoreB
        if (a < 0 || b < 0) return -1
        return a + b
    }

    val answeredCount: Int get() = listOf(e1, e2, gb, bms, bmc, dmh, vs, cf, co, hc).count { it >= 0 }
    val isComplete: Boolean get() = answeredCount == 10
}

data class IbpEvaluation(
    val id: String,
    val placetteId: String,
    val parcelleId: String,
    val observationDate: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val evaluatorName: String = "",
    val answers: IbpAnswers = IbpAnswers(),
    val globalNote: String = ""
) {
    val scoreA: Int get() = answers.scoreA
    val scoreB: Int get() = answers.scoreB
    val scoreTotal: Int get() = answers.scoreTotal
    val isComplete: Boolean get() = answers.isComplete

    fun levelColor(): IbpLevel = IbpLevel.fromScore(scoreTotal)
}

enum class IbpLevel(val minScore: Int, val maxScore: Int) {
    VERY_LOW(0, 4),
    LOW(5, 9),
    MEDIUM(10, 14),
    GOOD(15, 17),
    VERY_GOOD(18, 20);

    companion object {
        fun fromScore(score: Int): IbpLevel {
            if (score < 0) return VERY_LOW
            return values().lastOrNull { score >= it.minScore } ?: VERY_LOW
        }
    }
}
