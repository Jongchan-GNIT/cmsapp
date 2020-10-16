package kr.goodneighbors.cms.service.model

import android.arch.persistence.room.Ignore
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kr.goodneighbors.cms.service.entities.CD
import kr.goodneighbors.cms.service.entities.RPT_BSC

data class GmlListItem(
        var CHRCP_NO: String? = null,
        var MNG_NO: String? = null,
        var TYPE: String? = null,
        var RCP_DT: String? = null,
        var RELSH_CD: String? = null,
        var RCP_NO: String? = null,
        var YEAR: String? = null,
        var RPT_STCD: String? = null,
        var APRV_DT: String? = null,
        var FILE_PATH: String? = null,
        var SWRT_YN: String? = null,
        var RPT_STNM: String? = null
)

data class GmlEditItemSearch(var chrcp_no: String, var rcp_no: String?, var mng_no: String)

data class GmEditItem(
        var CHRCP_NO: String? = null,
        var MNG_NO: String? = null,
        var RELSH_CD: String? = null,
        var GIFT_DAMT: String? = null,
        var MBSH_REQ: String? = null,
        var YEAR: String? = null,
        var RCP_NO: String? = null,
        var RPT_STCD: String? = null,
        var PROF_RCP_NO: String? = null,
        var PROF_RPT_DVCD: String? = null,
        var PROF_YEAR: String? = null,
        var PROF_APRV_DT: String? = null,
        var RELMEM_NM: String? = null,
        var CHILD_NAME: String? = null,
        var CHILD_CODE: String? = null,
        var CH_STCD: String? = null,
        var GNDR: String? = null,
        var BDAY: String? = null,
        var VLG_CD: String? = null,
        var TEL_NO: String? = null,
        var AGE: String? = null,
        var SCTP_CD: String? = null,
        var SCHL_CD: String? = null,
        var GRAD: String? = null,
        var FA_LTYN: String? = null,
        var MO_LTYN: String? = null,
        var EBRO_LTNUM: String? = null,
        var ESIS_LTNUM: String? = null,
        var MGDN_CD: String? = null,
        var MGDN_NM: String? = null,
        var VLG_NM: String? = null,
        var VLG_LAT: String? = null,
        var VLG_LONG: String? = null,
        var SIBLING1: String? = null,
        var SIBLING2: String? = null,
        var SCTP_NM: String? = null,
        var SCHL_NM: String? = null,
        var MGDN_CD_NM: String? = null,
        var THUMB_FILE_PATH: String? = null,
        var FILE_PATH: String? = null,

        @Ignore var rpt_bsc: RPT_BSC? = null,
        @Ignore var gifts: List<GiftItem>? = null,
        @Ignore var returns: List<ReturnItem> ?= null
)

data class GiftItem(
        var master: CD,
        var detail: List<GiftItemDetail>
)

data class GiftItemDetail(
        var GRP_CD: String,
        var GIFT_BCD: String,
        var GIFT_SCD: String,
        var GIFT_DTBD: String? = null,
        var RCP_NO: String? = null,
        var SEQ_NO: String? = null,
        var GIFT_DAMT: Double? = null,
        var GIFT_NUM: Int? = null
)

@Parcelize
data class GiftConfirmData(
        var TITLE: String? = null,
        var COUNT: String? = null,
        var PRICE: String? = null,
        var TOTAL: String? = null
) : Parcelable

data class GmImageItem(
        var path: String? = null
)

//////////////////////////////// LETTER ///////////////////////////////////
data class GmLetterEditItem(
        var CHRCP_NO: String ?= null,
        var MNG_NO: String ?= null,
        var RELSH_CD_OLD: String ?= null,
        var RCP_DT: String ?= null,
        var SEND_DT: String ?= null,
        var RPLY_TMLMT: String ?= null,
        var ENCLO_ARTCL: String ?= null,
        var REGR_ID: String ?= null,
        var REG_DT: String ?= null,
        var UPDR_ID: String ?= null,
        var UPD_DT: String ?= null,
        var TRAN_ADCT: String ?= null,
        var TRAN_DVCD: String ?= null,
        var TRAN_STCD: String ?= null,
        var TRANR_NM: String ?= null,
        var REVW_NM: String ?= null,
        var RELMEM_DNCTR_CD: String ?= null,
        var RELSH_CD: String ?= null,
        var YEAR: String ?= null,
        var RCP_NO: String ?= null,
        var RPT_STCD: String ?= null,
        var PROF_RCP_NO: String ?= null,
        var PROF_RPT_DVCD: String ?= null,
        var PROF_YEAR: String ?= null,
        var PROF_APRV_DT: String ?= null,
        var RELMEM_NM: String ?= null,
        var CHILD_NAME: String ?= null,
        var CHILD_CODE: String ?= null,
        var CH_STCD: String ?= null,
        var GNDR: String ?= null,
        var BDAY: String ?= null,
        var VLG_CD: String ?= null,
        var TEL_NO: String ?= null,
        var AGE: String ?= null,
        var SCTP_CD: String ?= null,
        var SCHL_CD: String ?= null,
        var GRAD: String ?= null,
        var FA_LTYN: String ?= null,
        var MO_LTYN: String ?= null,
        var EBRO_LTNUM: String ?= null,
        var ESIS_LTNUM: String ?= null,
        var MGDN_CD: String ?= null,
        var MGDN_NM: String ?= null,
        var VLG_NM: String ?= null,
        var VLG_LAT: String ?= null,
        var VLG_LONG: String ?= null,
        var SIBLING1: String ?= null,
        var SIBLING2: String ?= null,
        var SCTP_NM: String ?= null,
        var SCHL_NM: String ?= null,
        var MGDN_CD_NM: String ?= null,
        var THUMB_FILE_PATH: String ?= null,
        var FILE_PATH: String ?= null,

        @Ignore var PREV_RPT_BSC: RPT_BSC ?= null,
        @Ignore var PREV_LETR_RPT_BSC: RPT_BSC ?= null,

        @Ignore var rpt_bsc: RPT_BSC? = null,

        @Ignore var codeRelationship:List<SpinnerOption>? = null,
        @Ignore var codeReason: List<SpinnerOption>? = null,

        @Ignore var returns: List<ReturnItem> ?= null
)