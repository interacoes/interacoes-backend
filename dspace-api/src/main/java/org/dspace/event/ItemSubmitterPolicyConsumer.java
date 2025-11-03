package org.dspace.event;

import org.dspace.authorize.factory.AuthorizeServiceFactory;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.content.Item;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;

import java.util.logging.Logger;

public class ItemSubmitterPolicyConsumer implements Consumer {

    private static final Logger log = Logger.getLogger(ItemSubmitterPolicyConsumer.class.getName());

    private final AuthorizeService authorizeService =
            AuthorizeServiceFactory.getInstance().getAuthorizeService();

    @Override
    public void initialize() throws Exception {
        // Nada necessário na inicialização
    }

    @Override
    public void consume(Context context, Event event) throws Exception {
        if (event.getSubjectType() == Constants.ITEM &&
                (event.getEventType() == Event.ADD ||
                        event.getEventType() == Event.MODIFY ||
                        event.getEventType() == Event.INSTALL)) {

            Item item = (Item) event.getSubject(context);
            EPerson submitter = item.getSubmitter();

            if (submitter != null) {
                // Adiciona permissões completas ao submitter sem remover heranças
                authorizeService.addPolicy(context, item, Constants.WRITE, submitter);
                authorizeService.addPolicy(context, item, Constants.DELETE, submitter);
                authorizeService.addPolicy(context, item, Constants.ADD, submitter);
                authorizeService.addPolicy(context, item, Constants.ADMIN, submitter);

                log.info("Submitter do item ID " + item.getID() +
                        " agora tem permissões de administrador do item: " + submitter.getEmail());
            } else {
                log.warning("Item ID " + item.getID() + " não possui submitter definido.");
            }
        }
    }

    @Override
    public void end(Context context) throws Exception {
        // Nada a finalizar
    }

    @Override
    public void finish(Context context) throws Exception {
        // Nada a finalizar
    }
}
