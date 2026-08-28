/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.converter;

import org.dspace.app.rest.model.EPersonRest;
import org.dspace.app.rest.projection.Projection;
import org.dspace.content.MetadataValue;
import org.dspace.eperson.EPerson;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * This is the converter from/to the EPerson in the DSpace API data model and the
 * REST data model
 *
 * @author Andrea Bollini (andrea.bollini at 4science.it)
 */
@Component
public class EPersonConverter extends DSpaceObjectConverter<EPerson, org.dspace.app.rest.model.EPersonRest> {

    @Override
    public EPersonRest convert(EPerson obj, Projection projection) {
        EPersonRest eperson = super.convert(obj, projection);
        eperson.setLastActive(obj.getLastActive());
        eperson.setNetid(obj.getNetid());
        eperson.setCanLogIn(obj.canLogIn());
        eperson.setRequireCertificate(obj.getRequireCertificate());
        eperson.setSelfRegistered(obj.getSelfRegistered());
        eperson.setEmail(obj.getEmail());
        setFullName(obj, eperson);
        setFuncao(obj, eperson);

        return eperson;
    }

    private void setFuncao(EPerson obj, EPersonRest eperson) {

        List<String> groupNames = obj.getGroups().stream()
                .map(group -> group.getName())
                .toList();

        String funcao = "anonimo";

        // Administrador do repositório
        if (groupNames.contains("Administrator")) {
            funcao = "administrador do repositorio";
        }

        // Administrador da comunidade
        else if (groupNames.stream()
                .anyMatch(groupName ->
                        groupName.startsWith("COMMUNITY_")
                                && groupName.endsWith("_ADMIN"))) {
            funcao = "administrador da comunidade";
        }

        // Curador
        else {
            boolean temSubmit = false;
            boolean temEditor = false;
            boolean temAdmin = false;

            for (String groupName : groupNames) {
                if (groupName.matches("COLLECTION_.+_SUBMIT")) {
                    temSubmit = true;
                }

                if (groupName.matches("COLLECTION_.+_WORKFLOW_ROLE_editor")) {
                    temEditor = true;
                }

                if (groupName.matches("COLLECTION_.+_ADMIN")) {
                    temAdmin = true;
                }
            }

            // Editor ou administrador de coleção = curador
            if (temEditor || temAdmin) {
                funcao = "curador";
            }
            // Apenas submissor = catalogador
            else if (temSubmit) {
                funcao = "catalogador";
            }
        }

        eperson.setFuncao(funcao);
    }

    private void setFullName(EPerson obj, EPersonRest eperson) {

        String firstName = "";
        String lastName = "";

        for (MetadataValue metadata : obj.getMetadata()) {

            String field = metadata.getMetadataField().getElement();

            if ("firstname".equals(field)) {
                firstName = metadata.getValue();
            }

            if ("lastname".equals(field)) {
                lastName = metadata.getValue();
            }
        }

        eperson.setFullName((firstName + " " + lastName).trim());
    }
    @Override
    protected EPersonRest newInstance() {
        return new EPersonRest();
    }

    @Override
    public Class<EPerson> getModelClass() {
        return EPerson.class;
    }

}
