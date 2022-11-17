package com.planus.db.repository;

import com.planus.db.entity.Timetable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TimetableRepository extends JpaRepository<Timetable, Long> {
    Optional<List<Timetable>> findByPlanPlanId(long planId);

    // 일별일정에 해당하는 모든 일정 삭제
    void deleteByPlanPlanId(long planId);

    // 일별일정에 해당하는 일정 저장

    void deleteAllByPlan_Trip_TripId(long tripId);
}
