package com.planus.trip.service;

import com.planus.db.entity.*;
import com.planus.db.repository.*;
import com.planus.trip.dto.TripAreaDTO;
import com.planus.trip.dto.TripInfoResDTO;
import com.planus.trip.dto.TripResDTO;
import com.planus.util.TokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TripServiceImpl implements TripService {
    private TokenProvider tokenProvider;
    private TripRepository tripRepository;
    private AreaRepository areaRepository;
    private TripAreaRepository tripAreaRepository;
    private UserRepository userRepository;
    private MemberRepository memberRepository;
    private PlanRepository planRepository;

    @Autowired
    public TripServiceImpl(TokenProvider tokenProvider, TripRepository tripRepository, AreaRepository areaRepository, TripAreaRepository tripAreaRepository, UserRepository userRepository, MemberRepository memberRepository, PlanRepository planRepository) {
        this.tokenProvider = tokenProvider;
        this.tripRepository = tripRepository;
        this.areaRepository = areaRepository;
        this.tripAreaRepository = tripAreaRepository;
        this.userRepository = userRepository;
        this.memberRepository = memberRepository;
        this.planRepository = planRepository;
    }

    @Override
    public TripResDTO createTrip(String token, String startDate, long period, int[] areaId) {
        long admin = tokenProvider.getUserId(token.split(" ")[1]);

        Trip trip = Trip.builder()
                .admin(admin)
                .createTime(LocalDateTime.now())
                .startDate(LocalDate.parse(startDate))
                .period(period)
                .tripUrl(UUID.randomUUID().toString())
                .build();

        tripRepository.save(trip);

        for (int i = 0; i < areaId.length; i++) {
            Area area = areaRepository.findByAreaId(areaId[i]);
            TripArea tripArea = TripArea.builder()
                    .trip(trip)
                    .area(area)
                    .build();

            tripAreaRepository.save(tripArea);
        }

        User user = userRepository.findByUserId(admin);

        Member member = Member.builder()
                .trip(trip)
                .user(user)
                .build();

        memberRepository.save(member);

        for (int i = 0; i <= period; i++) {
            LocalDate date = LocalDate.parse(startDate);
            Plan plan = Plan.builder()
                    .trip(trip)
                    .tripDate(date.plusDays(i))
                    .startTime(480)
                    .build();

            planRepository.save(plan);
        }

        TripResDTO tripResDTO = TripResDTO.builder()
                .tripId(trip.getTripId())
                .tripUrl(trip.getTripUrl())
                .build();

        return tripResDTO;
    }

    @Override
    public TripInfoResDTO findTripInfo(String token, String tripUrl) {
        Trip trip = tripRepository.findByTripUrl(tripUrl);
        List<TripAreaDTO> tripAreaDTOList = new ArrayList<>();
        List<TripArea> tripAreaList = tripAreaRepository.findByTripTripId(trip.getTripId());

        for (TripArea tripArea : tripAreaList) {
            Area area = areaRepository.findByAreaId(tripArea.getArea().getAreaId());
            TripAreaDTO tripAreaDTO = TripAreaDTO.builder()
                    .areaId(area.getAreaId())
                    .doName(area.getDoName())
                    .siName(area.getSiName())
                    .lat(area.getLat())
                    .lng(area.getLng())
                    .build();

            tripAreaDTOList.add(tripAreaDTO);
        }

        int memberOrAdmin;
        if (token == null) {
            memberOrAdmin = -1;
        } else if (trip.getAdmin() == tokenProvider.getUserId(token.split(" ")[1])) {
            memberOrAdmin = 2;
        } else if (memberRepository.existsByTripTripIdAndUserUserId(trip.getTripId(), tokenProvider.getUserId(token.split(" ")[1]))) {
            memberOrAdmin = 1;
        } else {
            memberOrAdmin = 0;
        }

        TripInfoResDTO tripInfoResDTO = TripInfoResDTO.builder()
                .tripId(trip.getTripId())
                .admin(trip.getAdmin())
                .startDate(trip.getStartDate().toString())
                .period(trip.getPeriod())
                .memberOrAdmin(memberOrAdmin)
                .complete(trip.isComplete())
                .imageUrl(trip.getImageUrl())
                .tripArea(tripAreaDTOList)
                .build();

        return tripInfoResDTO;
    }

    @Override
    public long changeAdmin(String token, long tripId, long userId) {
        Trip trip = tripRepository.findByTripId(tripId);
        Long adminId = trip.getAdmin();
        long requestUserId = tokenProvider.getUserId(token);

        if (adminId != requestUserId) {
            return -1;
        } else if (!memberRepository.existsByTripTripIdAndUserUserId(tripId, userId)) {
            return -1;
        } else {
            trip.changeAdmin(userId);
            return userId;
        }
    }

    @Override
    public boolean changeComplete(long tripId, boolean complete) {
        Trip PreTrip = tripRepository.findByTripId(tripId);
        Trip trip = Trip.builder()
                .tripId(PreTrip.getTripId())
                .tripUrl(PreTrip.getTripUrl())
                .admin(PreTrip.getAdmin())
                .createTime(PreTrip.getCreateTime())
                .startDate(PreTrip.getStartDate())
                .period(PreTrip.getPeriod())
                .complete(complete)
                .imageUrl(PreTrip.getImageUrl())
                .memberList(PreTrip.getMemberList())
                .bucketList(PreTrip.getBucketList())
                .tripAreaList(PreTrip.getTripAreaList())
                .planList(PreTrip.getPlanList())
                .build();
        tripRepository.save(trip);
        return true;
    }

    @Override
    public Trip findByTripId(long tripId) {
        return tripRepository.findByTripId(tripId);
    }

    @Override
    @Transactional
    public long changeAdminForWebSocket(Trip trip, long userId) {
        trip.changeAdmin(userId);
        tripRepository.save(trip);
        return userId;
    }
}
