/*
 * (c) Copyright 2015 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.view.idol.configuration;

import com.autonomy.aci.client.annotations.IdolAnnotationsProcessorFactory;
import com.autonomy.aci.client.services.AciService;
import com.hp.autonomy.frontend.configuration.ValidationResult;
import com.hp.autonomy.frontend.configuration.Validator;
import lombok.Setter;

@Setter
public class ViewConfigValidator implements Validator<ViewConfig> {

    private AciService testAciService;

    private IdolAnnotationsProcessorFactory idolAnnotationsProcessorFactory;

    @Override
    public ValidationResult<?> validate(final ViewConfig config) {
        return config.validate(testAciService, idolAnnotationsProcessorFactory);
    }

    @Override
    public Class<ViewConfig> getSupportedClass() {
        return ViewConfig.class;
    }
}
