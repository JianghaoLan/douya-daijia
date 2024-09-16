package org.lanjianghao.daijia.rules.config;

import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.io.Resource;
import org.kie.api.runtime.KieContainer;
import org.kie.internal.io.ResourceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class DroolsConfig {

    private static final String FEE_RULES_DRL = "rules/FeeRule.drl";
    private static final String REWARD_RULES_DRL = "rules/RewardRule.drl";
    private static final String PROFIT_SHARING_DRL = "rules/ProfitsharingRule.drl";
    private static final String FEE_PACKAGE = "org.lanjianghao.daijia.fee";
    private static final String REWARD_PACKAGE = "org.lanjianghao.daijia.reward";
    private static final String PROFIT_SHARING_PACKAGE = "org.lanjianghao.daijia.profitsharing";

    public static final String FEE_SESSION_NAME = "FeeSession";
    public static final String REWARD_SESSION_NAME = "RewardSession";
    public static final String PROFIT_SHARING_SESSION_NAME = "ProfitSharingSession";

    private void buildKModuleModel(KieModuleModel kieModuleModel) {
        KieBaseModel feeBase = kieModuleModel.newKieBaseModel("FeeBase").addPackage(FEE_PACKAGE);
        feeBase.newKieSessionModel(FEE_SESSION_NAME).setDefault(true).setType(KieSessionModel.KieSessionType.STATELESS);

        KieBaseModel rewardBase = kieModuleModel.newKieBaseModel("RewardBase").addPackage(REWARD_PACKAGE);
        rewardBase.newKieSessionModel(REWARD_SESSION_NAME).setDefault(true).setType(KieSessionModel.KieSessionType.STATELESS);

        KieBaseModel profitSharingBase = kieModuleModel.newKieBaseModel("ProfitSharingBase").addPackage(PROFIT_SHARING_PACKAGE);
        profitSharingBase.newKieSessionModel(PROFIT_SHARING_SESSION_NAME).setDefault(true).setType(KieSessionModel.KieSessionType.STATELESS);
    }

    private List<Resource> getResources() {
        return Arrays.asList(
                ResourceFactory.newClassPathResource(FEE_RULES_DRL),
                ResourceFactory.newClassPathResource(REWARD_RULES_DRL),
                ResourceFactory.newClassPathResource(PROFIT_SHARING_DRL)
        );
    }

    @Bean
    public KieContainer kieContainer() {
        KieServices kieServices = KieServices.Factory.get();

        KieModuleModel kieModuleModel = kieServices.newKieModuleModel();
        buildKModuleModel(kieModuleModel);

        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        for (Resource res : getResources()) {
            kieFileSystem.write(res);
        }
        kieFileSystem.writeKModuleXML(kieModuleModel.toXML());

        KieBuilder kb = kieServices.newKieBuilder(kieFileSystem);
        kb.buildAll();

        KieModule kieModule = kb.getKieModule();

        return kieServices.newKieContainer(kieModule.getReleaseId());
    }
}
