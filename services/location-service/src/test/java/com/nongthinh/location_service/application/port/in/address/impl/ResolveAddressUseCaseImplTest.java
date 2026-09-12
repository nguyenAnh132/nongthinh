package com.nongthinh.location_service.application.port.in.address.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.nongthinh.location_service.application.port.out.repository.CommuneRepository;
import com.nongthinh.location_service.application.port.out.repository.ProvinceRepository;
import com.nongthinh.location_service.domain.commune.Commune;
import com.nongthinh.location_service.domain.province.Province;

@ExtendWith(MockitoExtension.class)
class ResolveAddressUseCaseImplTest {

    @Mock
    private ProvinceRepository provinceRepository;

    @Mock
    private CommuneRepository communeRepository;

    @InjectMocks
    private ResolveAddressUseCaseImpl useCase;

    @Test
    void returnsProvinceAndCommuneNamesForAValidAddress() {
        String provinceId = "01";
        UUID communeId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        when(provinceRepository.findById(provinceId))
                .thenReturn(Optional.of(Province.reconstruct(provinceId, "HN", "Thành phố Hà Nội")));
        when(communeRepository.findById(communeId))
                .thenReturn(Optional.of(Commune.reconstruct(
                        communeId,
                        provinceId,
                        "00001",
                        "Phường Ba Đình"
                )));

        var result = useCase.execute(provinceId, communeId);

        assertThat(result.provinceName()).isEqualTo("Thành phố Hà Nội");
        assertThat(result.communeName()).isEqualTo("Phường Ba Đình");
    }
}
