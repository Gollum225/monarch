package controller.rules;

import controller.Rule;
import controller.RuleType;
import exceptions.CloneProhibitedException;
import model.Repository;
import model.RuleReturn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileExtensions extends Rule {

    public FileExtensions(Repository repository) {
        super(RuleType.MANDATORY, repository);
    }

    static final List<String> fileExtensions = new ArrayList<>(Arrays.asList(".uml", ".xmi", ".ebm", ".eap"));

    static final int[] limits = {1, 3, 5, 7, 10};

    @Override
    public RuleReturn execute() {

        String structure;
        try {
            structure = repository.getStructure().toString();
        } catch (CloneProhibitedException e) {
            return new RuleReturn(e.getMessage(), repository.getIdentifier(), this.getClass().getSimpleName());
        }

        int count = countMultipleKeywordsMultipleOccurrence(structure, fileExtensions);

        for (int i = limits.length - 1; i >= 0; i--) {
            if (count >= limits[i]) {
                return new RuleReturn(i+1);
            }
        }
        return new RuleReturn(0);

    }
}
