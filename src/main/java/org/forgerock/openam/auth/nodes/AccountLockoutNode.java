/*
 * simon.moffatt@forgerock.com
 *
 * Locks account 
 *
 */

//package org.forgerock.openam.auth.nodes;
package org.forgerock.openam.auth.nodes;

import java.util.List;
import java.util.ResourceBundle;

import javax.inject.Inject;

import org.forgerock.guava.common.collect.ImmutableList;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.annotations.sm.Attribute;
import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.Node;
import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.forgerock.util.i18n.PreferredLocales;

import com.google.inject.assistedinject.Assisted;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.forgerock.openam.auth.node.api.SharedStateConstants.REALM;
import static org.forgerock.openam.auth.node.api.SharedStateConstants.USERNAME;

import org.forgerock.openam.core.CoreWrapper;
import com.sun.identity.idm.AMIdentity;
import com.sun.identity.idm.IdRepoException;
import com.iplanet.sso.SSOException;


@Node.Metadata(outcomeProvider = AccountLockoutNode.OutcomeProvider.class,
        configClass = AccountLockoutNode.Config.class)
public class AccountLockoutNode implements Node {


    private final Logger logger = LoggerFactory.getLogger("amAuth");

    /**
     * Configuration for the node.
     */

    public interface Config {

    }

    private final CoreWrapper coreWrapper;

    private final Config config;

    /**
     * Create the node.
     * @param config The service config.
     * @throws NodeProcessException If the configuration was not valid.
     */
    @Inject
    public AccountLockoutNode(@Assisted Config config, CoreWrapper coreWrapper) throws NodeProcessException {
        this.config = config;
        this.coreWrapper = coreWrapper;
    }

    @Override
    public Action process(TreeContext context) throws NodeProcessException {
	
	logger.info("AccountLockoutNode entered");

    //Retrieve Identity ////////////////////////////////////////////////////////////////////////////////////////////
    //Uses the utility wrapper in CoreWrapper to search for identity. Uses contants from shared state
    AMIdentity userIdentity = coreWrapper.getIdentity(context.sharedState.get(USERNAME).asString(),context.sharedState.get(REALM).asString());

    //Set to false (aka inetuserstatus=Inactive)
    try {
        
        userIdentity.setActiveStatus(false);
    
    } catch (IdRepoException | SSOException e) {
       
        throw new NodeProcessException(e);

    }
    
	
	//All other OS's spin to other
	return goTo("Locked").build();

    }

    private Action.ActionBuilder goTo(String outcome) {
        return Action.goTo(outcome);
    }

    static final class OutcomeProvider implements org.forgerock.openam.auth.node.api.OutcomeProvider {
        private static final String BUNDLE = AccountLockoutNode.class.getName().replace(".", "/");

        @Override
        public List<Outcome> getOutcomes(PreferredLocales locales, JsonValue nodeAttributes) {
            ResourceBundle bundle = locales.getBundleInPreferredLocale(BUNDLE, OutcomeProvider.class.getClassLoader());
            return ImmutableList.of(
            new Outcome("Locked", bundle.getString("Locked")));
        }
    }
}
