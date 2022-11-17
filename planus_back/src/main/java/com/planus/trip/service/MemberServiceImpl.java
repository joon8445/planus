package com.planus.trip.service;

import com.planus.db.entity.Member;
import com.planus.db.repository.MemberRepository;
import com.planus.db.repository.TripRepository;
import com.planus.db.repository.UserRepository;
import com.planus.trip.dto.MemberResDTO;
import com.planus.util.RedisUtil;
import com.planus.util.TokenProvider;
import com.planus.websocket.model.WebSocketMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MemberServiceImpl implements MemberService{
    private TokenProvider tokenProvider;
    private MemberRepository memberRepository;
    private UserRepository userRepository;
    private TripRepository tripRepository;
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    public MemberServiceImpl(TokenProvider tokenProvider, MemberRepository memberRepository, UserRepository userRepository, TripRepository tripRepository, RedisTemplate<String, String> redisTemplate){
        this.tokenProvider = tokenProvider;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
        this.tripRepository = tripRepository;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public long addMember(String token, long tripId) {
        if(token!=null && tokenProvider.getUserId(token.split(" ")[1])!=-1 && userRepository.findByUserId(tokenProvider.getUserId(token.split(" ")[1]))!=null){
            if(memberRepository.existsByTripTripIdAndUserUserId(tripId, tokenProvider.getUserId(token.split(" ")[1]))){
                return -2;
            }else if(memberRepository.countByTripTripId(tripId)>9){
                return -1;
            }else{
                Member member = Member.builder()
                        .user(userRepository.findByUserId(tokenProvider.getUserId(token.split(" ")[1])))
                        .trip(tripRepository.findByTripId(tripId))
                        .build();

                memberRepository.save(member);

                return member.getMemberId();
            }
        }
        return 0;
    }

    @Override
    public List<MemberResDTO> findMemberList(long tripId) {
        List<MemberResDTO> memberResDTOList = new ArrayList<>();
        List<Member> memberList = memberRepository.findByTripTripId(tripId);
        for(Member member : memberList){
            MemberResDTO memberResDTO = MemberResDTO.builder()
                    .userId(member.getUser().getUserId())
                    .name(member.getUser().getName())
                    .email(member.getUser().getEmail())
                    .build();

            memberResDTOList.add(memberResDTO);
        }

        return memberResDTOList;
    }

    @Override
    public void addConnector(WebSocketMember member) {
        String key = "connectorList::" + member.getTripId();
        String subKey = String.valueOf(tokenProvider.getUserId(member.getToken().split(" ")[1]));

        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();

        if (hashOperations.get(key, subKey)==null || Integer.parseInt(hashOperations.get(key, subKey))<=0){
            hashOperations.put(key,subKey, String.valueOf(1));
        }else {
            hashOperations.increment(key, subKey, 1);
        }
    }

    @Override
    public void delConnector(WebSocketMember member) {
        String key = "connectorList::" + member.getTripId();
        String subKey = String.valueOf(tokenProvider.getUserId(member.getToken().split(" ")[1]));

        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();

        if (hashOperations.get(key, subKey)!=null){
            hashOperations.increment(key, subKey, -1);
        }

        if (Integer.parseInt(hashOperations.get(key, subKey))<=0){
            hashOperations.delete(key, subKey);
        }
    }

    @Override
    public Object[] getConnector(WebSocketMember member) {
        String key = "connectorList::" + member.getTripId();

        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();

        Map map = hashOperations.entries(key);


        Object[] array = map.keySet().toArray();

        return array;
    }

    @Override
    public Member findMember(long memberId) {
        return memberRepository.findOneByMemberId(memberId);
    }

    @Override
    public boolean isMemberOfTrip(long tripId, long userId) {
        return memberRepository.existsByTripTripIdAndUserUserId(tripId, userId);
    }
}
