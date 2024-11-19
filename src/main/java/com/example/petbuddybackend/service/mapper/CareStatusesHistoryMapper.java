package com.example.petbuddybackend.service.mapper;

import com.example.petbuddybackend.dto.care.CareStatusesHistoryDTO;
import com.example.petbuddybackend.entity.care.CareStatusesHistory;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Mapper
public interface CareStatusesHistoryMapper {

    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "mapToZonedDateTime")
    CareStatusesHistoryDTO mapToCareStatusesHistoryDTO(CareStatusesHistory careStatusesHistory, @Context ZoneId zoneId);

    @Named("mapToZonedDateTime")
    default ZonedDateTime mapToZonedDateTime(ZonedDateTime date, @Context ZoneId zoneId) {
        return date.withZoneSameInstant(zoneId);
    }
}
