package tech.qhuyy.hqngEconomy.expansion

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer
import tech.qhuyy.hqngEconomy.api.EconomyAPI
import tech.qhuyy.hqngEconomy.api.EconomyProvider
import tech.qhuyy.hqngEconomy.util.NumberUtil.formatCompact
import tech.qhuyy.hqngEconomy.util.NumberUtil.formatGems
import tech.qhuyy.hqngEconomy.util.NumberUtil.formatMoney
/**
 * PlaceholderAPI expansion for HqngEconomy.
 *
 * Supported placeholders:
 *   %economy_money%                 — raw money balance (1250.5)
 *   %economy_money_formatted%       — formatted money (1,250.50)
 *   %economy_money_compact%         — compact money (1.2K)
 *   %economy_gem%                   — raw gem balance (7)
 *   %economy_gem_formatted%         — formatted gem (7)
 *   %economy_has_money_<amount>%    — YES / NO
 *   %economy_has_gem_<amount>%      — YES / NO
 */
class PlaceholderExpansion : PlaceholderExpansion() {

    private val provider: EconomyProvider
        get() = EconomyAPI.provider

    // ── Expansion identity ────────────────────────────────────────

    override fun getIdentifier(): String = "HqngEconomy"

    override fun getAuthor(): String = "hqng05"

    override fun getVersion(): String = "1.0.0"

    /** Persist across PAPI reloads so we don't re-register. */
    override fun persist(): Boolean = true

    /** Register on PAPI's reload too. */
    override fun canRegister(): Boolean = EconomyAPI.isAvailable

    // ── Placeholder handler ───────────────────────────────────────

    override fun onRequest(player: OfflinePlayer?, params: String): String {
        val uuid = player?.uniqueId ?: return ""

        return when {
            // ── Money ──────────────────────────────────────────────
            params == "money" ->
                provider.getBalance(uuid).toString()

            params == "money_formatted" ->
                formatMoney(provider.getBalance(uuid))

            params == "money_compact" ->
                formatCompact(provider.getBalance(uuid))

            // ── Gem ────────────────────────────────────────────────
            params == "gem" ->
                provider.getGems(uuid).toString()

            params == "gem_formatted" ->
                formatGems(provider.getGems(uuid))

            // ── Has-money check: %economy_has_money_<amount>% ──────
            params.startsWith("has_money_") -> {
                val amountStr = params.removePrefix("has_money_")
                val amount = amountStr.toDoubleOrNull() ?: return "ERROR"
                if (provider.getBalance(uuid) >= amount) "YES" else "NO"
            }

            // ── Has-gem check: %economy_has_gem_<amount>% ──────────
            params.startsWith("has_gem_") -> {
                val amountStr = params.removePrefix("has_gem_")
                val amount = amountStr.toIntOrNull() ?: return "ERROR"
                if (provider.getGems(uuid) >= amount) "YES" else "NO"
            }

            // ── Unknown ────────────────────────────────────────────
            else -> ""
        }
    }
}
