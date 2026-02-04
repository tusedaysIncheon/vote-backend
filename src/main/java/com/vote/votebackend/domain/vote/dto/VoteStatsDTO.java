package com.vote.votebackend.domain.vote.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VoteStatsDTO {

    private Long id;
    private Long totalVoteCount;
    private Map<Long, Long> optionCount;
    private Map<Long, Map<String, Map<String, Long>>> detailedStats;


    public static VoteStatsDTO toRedisMap(Long voteId, Map<Object, Object> rawMap) {

        Long totalVoteCount = 0L;
        Map<Long, Long> optionCount = new HashMap<>();

        Map<Long, Map<String, Map<String, Long>>> detailedStats = new HashMap<>();

        if(rawMap == null || rawMap.isEmpty()){

            return VoteStatsDTO.builder()
                    .id(voteId)
                    .totalVoteCount(0L)
                    .optionCount(new HashMap<>())
                    .detailedStats(new HashMap<>())
                    .build();
        }

        for(Map.Entry<Object, Object> entry : rawMap.entrySet()){
            String key = (String) entry.getKey();
            Long count = Long.parseLong((String) entry.getValue());

            if("total".equals(key)){
                totalVoteCount = count;
                continue;
            }

            String [] parts = key.split(":");
            if(parts.length != 2 || !"opt".equals(parts[0])) continue;

            try{
                Long optionId = Long.parseLong(parts[1]);

                if(parts.length == 2){
                    optionCount.put(optionId, count);
                } else if(parts.length == 4){
                    String category = parts[2];
                    String value = parts[3];

                    detailedStats.computeIfAbsent(optionId, k -> new HashMap<>())
                            .computeIfAbsent(category, k -> new HashMap<>())
                            .put(value, count);
                }
            } catch ( NumberFormatException e ){
                continue;
            }
        }
        return VoteStatsDTO.builder()
                .id(voteId)
                .totalVoteCount(totalVoteCount)
                .optionCount(optionCount)
                .detailedStats(detailedStats)
                .build();
    }
}
