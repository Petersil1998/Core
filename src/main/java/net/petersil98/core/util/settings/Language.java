package net.petersil98.core.util.settings;

/**
 * Enumeration of all possible Languages supported by Data Dragon
 */
public enum Language {
    AR_AE("ar_AE"),
    CS_CZ("cs_CZ"),
    DE_DE("de_DE", true),
    EL_GR("el_GR"),
    EN_AU("en_AU"),
    EN_GB("en_GB"),
    EN_PH("en_PH"),
    EN_SG("en_SG"),
    EN_US("en_US", true),
    ES_AR("es_AR"),
    ES_ES("es_ES", true),
    ES_MX("es_MX", true),
    FR_FR("fr_FR", true),
    HU_HU("hu_HU"),
    IT_IT("it_IT", true),
    JA_JP("ja_JP", true),
    KO_KR("ko_KR", true),
    PL_PL("pl_PL", true),
    PT_BR("pt_BR", true),
    RO_RO("ro_RO"),
    RU_RU("ru_RU", true),
    TH_TH("th_TH", true),
    TR_TR("tr_TR", true),
    VI_VN("vi_VN"),
    ZH_CN("zh_CN"),
    ZH_MY("zh_MY"),
    ZH_TW("zh_TW", true);

    final String name;
    final boolean availableForLor;

    Language(String name) {
        this(name, false);
    }

    Language(String name, boolean availableForLor) {
        this.name = name;
        this.availableForLor = availableForLor;
    }

    public boolean isAvailableForLor() {
        return availableForLor;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
