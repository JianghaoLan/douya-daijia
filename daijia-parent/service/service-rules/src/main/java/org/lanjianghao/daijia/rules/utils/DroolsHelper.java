package org.lanjianghao.daijia.rules.utils;

import org.kie.api.command.Command;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.internal.command.CommandFactory;
import org.lanjianghao.daijia.model.form.rules.FeeRuleRequest;
import org.lanjianghao.daijia.model.form.rules.ProfitsharingRuleRequest;
import org.lanjianghao.daijia.model.form.rules.RewardRuleRequest;
import org.lanjianghao.daijia.model.vo.rules.FeeRuleResponse;
import org.lanjianghao.daijia.model.vo.rules.FeeRuleResponseVo;
import org.lanjianghao.daijia.model.vo.rules.ProfitsharingRuleResponse;
import org.lanjianghao.daijia.model.vo.rules.RewardRuleResponse;
import org.lanjianghao.daijia.rules.config.DroolsConfig;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DroolsHelper {
    @Autowired
    private KieContainer kieContainer;

    public FeeRuleResponse calculateOrderFee(FeeRuleRequest feeRuleRequest) {
        StatelessKieSession kieSession = kieContainer.newStatelessKieSession(DroolsConfig.FEE_SESSION_NAME);

        FeeRuleResponse feeRuleResponse = new FeeRuleResponse();

        List<Command<?>> cmds = new ArrayList<>();
        cmds.add(CommandFactory.newSetGlobal("feeRuleResponse", feeRuleResponse));
        cmds.add(CommandFactory.newInsert(feeRuleRequest, "feeRuleRequest"));

        // 开启会话
        // Execute the list.
        kieSession.execute(CommandFactory.newBatchExecution(cmds));

        return feeRuleResponse;
    }

    public RewardRuleResponse calculateReward(RewardRuleRequest request) {
        StatelessKieSession kieSession = kieContainer.newStatelessKieSession(DroolsConfig.REWARD_SESSION_NAME);

        RewardRuleResponse response = new RewardRuleResponse();

        List<Command<?>> cmds = new ArrayList<>();
        cmds.add(CommandFactory.newSetGlobal("rewardRuleResponse", response));
        cmds.add(CommandFactory.newInsert(request, "rewardRuleRequest"));
        kieSession.execute(CommandFactory.newBatchExecution(cmds));

        return response;
    }

    public ProfitsharingRuleResponse calculateProfitsharingFee(ProfitsharingRuleRequest request) {
        StatelessKieSession kieSession = kieContainer.newStatelessKieSession(DroolsConfig.PROFIT_SHARING_SESSION_NAME);

        ProfitsharingRuleResponse response = new ProfitsharingRuleResponse();

        List<Command<?>> cmds = new ArrayList<>();
        cmds.add(CommandFactory.newSetGlobal("profitsharingRuleResponse", response));
        cmds.add(CommandFactory.newInsert(request, "profitsharingRuleRequest"));
        kieSession.execute(CommandFactory.newBatchExecution(cmds));

        return response;
    }
}
