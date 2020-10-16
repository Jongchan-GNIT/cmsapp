package kr.goodneighbors.cms.di

import dagger.Component
import kr.goodneighbors.cms.service.viewmodel.AclViewModel
import kr.goodneighbors.cms.service.viewmodel.ChildlistViewModel
import kr.goodneighbors.cms.service.viewmodel.CifViewModel
import kr.goodneighbors.cms.service.viewmodel.CommonViewModel
import kr.goodneighbors.cms.service.viewmodel.DeviceVerifyViewModel
import kr.goodneighbors.cms.service.viewmodel.GmlViewModel
import kr.goodneighbors.cms.service.viewmodel.HomeViewModel
import kr.goodneighbors.cms.service.viewmodel.ProfileViewModel
import kr.goodneighbors.cms.service.viewmodel.ProvidedServiceViewModel
import kr.goodneighbors.cms.service.viewmodel.ReportViewModel
import kr.goodneighbors.cms.service.viewmodel.SettingViewModel
import kr.goodneighbors.cms.service.viewmodel.StatisticsViewModel
import kr.goodneighbors.cms.service.viewmodel.SyncViewModel
import kr.goodneighbors.cms.service.viewmodel.UserInfoViewModel
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, DataModule::class, NetworkModule::class])
interface AppComponent {
    fun inject(into: DeviceVerifyViewModel)

    fun inject(into: UserInfoViewModel)

    fun inject(into: ReportViewModel)

    fun inject(into: ChildlistViewModel)

    fun inject(into: CifViewModel)

    fun inject(into: CommonViewModel)

    fun inject(into: SyncViewModel)

    fun inject(into: AclViewModel)

    fun inject(into: ProfileViewModel)

    fun inject(into: ProvidedServiceViewModel)

    fun inject(into: GmlViewModel)

    fun inject(into: HomeViewModel)

    fun inject(into: StatisticsViewModel)

    fun inject(into: SettingViewModel)
}
