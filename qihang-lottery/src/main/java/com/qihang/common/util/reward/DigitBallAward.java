package com.qihang.common.util.reward;

import com.qihang.domain.permutation.PermutationAwardDO;
import lombok.Data;

import java.util.List;
import java.util.concurrent.Callable;


@Data
public class DigitBallAward implements Callable<List<DigitBall>> {


    List<DigitBall> balls;
    PermutationAwardDO award;

    public DigitBallAward(List<DigitBall> balls, PermutationAwardDO award) {
        this.balls = balls;
        this.award = award;
    }

    @Override
    public List<DigitBall> call() {
        DigitBallAwardUtils.award(balls, award);
        return balls;
    }
}
