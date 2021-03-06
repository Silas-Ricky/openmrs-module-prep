/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.prep.reporting.data.converter.definition.evaluator.prep;

import org.openmrs.annotation.Handler;

import org.openmrs.module.prep.reporting.data.converter.definition.prep.AssessedDataDefinition;
import org.openmrs.module.reporting.data.person.EvaluatedPersonData;
import org.openmrs.module.reporting.data.person.definition.PersonDataDefinition;
import org.openmrs.module.reporting.data.person.evaluator.PersonDataEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * Evaluates PersonDataDefinition
 */
@Handler(supports = AssessedDataDefinition.class, order = 50)
public class AssessedDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);
		
		String qry = "select e.patient_id,if(ba.visit_date>=f.visit_date or ba.visit_date>=r.visit_date,'Yes','No') as assessed from kenyaemr_etl.etl_prep_enrolment e\n"
		        + "                left outer join (select patient_id,max(visit_date) as visit_date from kenyaemr_etl.etl_prep_followup group by patient_id) f on e.patient_id =f.patient_id\n"
		        + "left outer join (select patient_id,max(visit_date) as visit_date from kenyaemr_etl.etl_prep_monthly_refill group by patient_id) r on e.patient_id = r.patient_id\n"
		        + "left outer join (select patient_id,max(visit_date) as visit_date from kenyaemr_etl.etl_prep_behaviour_risk_assessment group by patient_id)ba on e.patient_id = ba.patient_id group by e.patient_id;";
		
		SqlQueryBuilder queryBuilder = new SqlQueryBuilder();
		queryBuilder.append(qry);
		Map<Integer, Object> data = evaluationService.evaluateToMap(queryBuilder, Integer.class, Object.class, context);
		c.setData(data);
		return c;
	}
}
