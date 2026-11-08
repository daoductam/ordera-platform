package com.ordera.modules.reward.application.usecase;

import com.ordera.modules.reward.presentation.dto.MyRewardsWalletResponse;

public interface GetMyRewardsWalletUseCase {

    MyRewardsWalletResponse execute(String status, String type);
}
