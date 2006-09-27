package org.riotfamily.riot.hibernate.job;

import java.sql.SQLException;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.riotfamily.riot.job.persistence.JobDao;
import org.riotfamily.riot.job.persistence.JobDetail;
import org.riotfamily.riot.job.persistence.JobLogEntry;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class HibernateJobDao extends HibernateDaoSupport implements JobDao {

	public List getJobDetails() {
		return getHibernateTemplate().find("from JobDetail job " +
				"order by job.endDate desc");
	}
	
	public List getPendingJobDetails() {
		return getHibernateTemplate().find("from JobDetail job where " +
				"job.state != " + JobDetail.CANCELED + " and " +
				"job.state != " + JobDetail.COMPLETED +
				"order by job.startDate desc");
	}
	
	public JobDetail getPendingJobDetail(String type, String objectId) {
		List jobs = getHibernateTemplate().find("from JobDetail job where " +
				"job.state != " + JobDetail.CANCELED + " and " +
				"job.state != " + JobDetail.COMPLETED +
				"order by job.startDate desc");
		
		if (jobs.isEmpty()) {
			return null;
		}
		return (JobDetail) jobs.get(0);
	}
	
	public int getAverageStepTime(final String type) {
		Object time = getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) 
					throws HibernateException, SQLException {
				
				Query q = session.createQuery(
						"select avg(averageStepTime) " +
						"from JobDetail where stepsCompleted > 0 and " +
						"type = :type");
				
				q.setParameter("type", type);
				return q.uniqueResult();
			}
		});
		if (time == null) {
			return 0;
		}
		return ((Number) time).intValue();
	}
	
	public JobDetail getJobDetail(Long id) {
		return (JobDetail) getHibernateTemplate().load(JobDetail.class, id);
	}
	
	public void saveJobDetail(JobDetail job) {
		getHibernateTemplate().save(job);
	}

	public void updateJobDetail(JobDetail job) {
		getHibernateTemplate().update(job);
	}

	public List getLogEntries(Long jobId) {
		return getHibernateTemplate().find("from JobLogEntry e where " +
				"e.job.id = ? order by e.date desc", jobId);
	}

	public void log(JobLogEntry entry) {
		getHibernateTemplate().save(entry);
	}

}
