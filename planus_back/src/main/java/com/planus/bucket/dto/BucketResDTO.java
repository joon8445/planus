package com.planus.bucket.dto;

import com.planus.db.entity.Bucket;
import lombok.*;

@Getter
@NoArgsConstructor
@ToString
public class BucketResDTO {


    private String place;

    private String address;

    private Double lat;

    private Double lng;


    @Builder
    public BucketResDTO(String place, String address, Double lat, Double lng) {
        //this.bucketId = bucketId;
        this.place = place;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
    }

    public static BucketResDTO toResDTO(Bucket bucket) {
        return BucketResDTO.builder()
                //.bucketId(bucket.getBucketId())
                .place(bucket.getPlace())
                .address(bucket.getAddress())
                .lat(bucket.getLat())
                .lng(bucket.getLng())
                .build();
    }

}
