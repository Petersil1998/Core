package net.petersil98.core.util.settings;

/**
 * Enumeration of all possible Languages supported by Data Dragon
 */
public enum Language {
    AR_AE("ar_AE", false, true),
    CS_CZ("cs_CZ", false, false),
    DE_DE("de_DE", true, true),
    EL_GR("el_GR", false, false),
    EN_AU("en_AU", false, false),
    EN_GB("en_GB", false, false),
    EN_PH("en_PH", false, false),
    EN_SG("en_SG", false, false),
    EN_US("en_US", true, true),
    ES_AR("es_AR", false, false),
    ES_ES("es_ES", true, true),
    ES_MX("es_MX", true, true),
    FR_FR("fr_FR", true, true),
    HU_HU("hu_HU", false, false),
    IT_IT("it_IT", true, true),
    JA_JP("ja_JP", true, true),
    KO_KR("ko_KR", true, true),
    PL_PL("pl_PL", true, true),
    PT_BR("pt_BR", true, true),
    RO_RO("ro_RO", false, false),
    RU_RU("ru_RU", true, true),
    TH_TH("th_TH", true, true),
    TR_TR("tr_TR", true, true),
    VI_VN("vi_VN", false, true),
    ZH_CN("zh_CN", false, true),
    ZH_MY("zh_MY", false, false),
    ZH_TW("zh_TW", true, true);

    final String name;
    final boolean availableForLor;
    final boolean availableForVal;

    Language(String name, boolean availableForLor, boolean availableForVal) {
        this.name = name;
        this.availableForLor = availableForLor;
        this.availableForVal = availableForVal;
    }

    public boolean isAvailableForLor() {
        return availableForLor;
    }

    public boolean isAvailableForVal() {
        return availableForVal;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
