package cms.repository.vote.impl;

import cms.dto.QueryResult;
import cms.model.vote.VoteOption;
import cms.model.vote.VoteRecord;
import cms.model.vote.VoteTheme;
import cms.repository.besa.DaoSupport;
import cms.repository.vote.VoteRepository;
import jakarta.persistence.Query;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Repository
@Transactional
public class VoteRepositoryImpl extends DaoSupport<VoteTheme> implements VoteRepository {

    private static final Logger logger = LogManager.getLogger(VoteRepositoryImpl.class);

    @Override
    @Transactional(readOnly = true, propagation = Propagation.NOT_SUPPORTED)
    public VoteTheme findVoteThemeById(String voteThemeId) {
        if (voteThemeId == null || voteThemeId.trim().isEmpty()) {
            return null;
        }
        Query query = em.createQuery("select o from VoteTheme o where o.id=?1")
                .setParameter(1, voteThemeId);
        List<VoteTheme> list = query.getResultList();
        for (VoteTheme p : list) {
            return p;
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.NOT_SUPPORTED)
    public VoteTheme findVoteThemeBySourceParameterId(Integer module, String sourceParameterId) {
        if (sourceParameterId == null || sourceParameterId.trim().isEmpty()) {
            return null;
        }
        Query query = em.createQuery("select o from VoteTheme o where o.module=?1 and o.sourceParameterId=?2")
                .setParameter(1, module)
                .setParameter(2, sourceParameterId);
        List<VoteTheme> list = query.getResultList();
        for (VoteTheme p : list) {
            return p;
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.NOT_SUPPORTED)
    public List<VoteOption> findVoteOptionByVoteThemeId(String voteThemeId) {
        if (voteThemeId == null || voteThemeId.trim().isEmpty()) {
            return new ArrayList<>();
        }
        Query query = em.createQuery("select o from VoteOption o where o.voteThemeId=?1 order by o.sort asc")
                .setParameter(1, voteThemeId);
        return query.getResultList();
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.NOT_SUPPORTED)
    public VoteOption findVoteOptionById(String voteOptionId) {
        if (voteOptionId == null || voteOptionId.trim().isEmpty()) {
            return null;
        }
        Query query = em.createQuery("select o from VoteOption o where o.id=?1")
                .setParameter(1, voteOptionId);
        List<VoteOption> list = query.getResultList();
        for (VoteOption p : list) {
            return p;
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.NOT_SUPPORTED)
    public VoteRecord findVoteRecordByUserAndTheme(String userName, String voteThemeId) {
        if (userName == null || userName.trim().isEmpty() || voteThemeId == null || voteThemeId.trim().isEmpty()) {
            return null;
        }
        Query query = em.createQuery("select o from VoteRecord o where o.userName=?1 and o.voteThemeId=?2")
                .setParameter(1, userName)
                .setParameter(2, voteThemeId);
        List<VoteRecord> list = query.getResultList();
        for (VoteRecord p : list) {
            return p;
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.NOT_SUPPORTED)
    public List<VoteRecord> findVoteRecordByVoteThemeId(String voteThemeId) {
        if (voteThemeId == null || voteThemeId.trim().isEmpty()) {
            return new ArrayList<>();
        }
        Query query = em.createQuery("select o from VoteRecord o where o.voteThemeId=?1 order by o.voteTime desc")
                .setParameter(1, voteThemeId);
        return query.getResultList();
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.NOT_SUPPORTED)
    public Map<String, Long> countVoteByVoteThemeId(String voteThemeId) {
        Map<String, Long> result = new HashMap<>();
        if (voteThemeId == null || voteThemeId.trim().isEmpty()) {
            return result;
        }
        Query query = em.createQuery("select o.voteOptionId, count(o) from VoteRecord o where o.voteThemeId=?1 group by o.voteOptionId")
                .setParameter(1, voteThemeId);
        List<Object[]> list = query.getResultList();
        for (Object[] obj : list) {
            String optionId = (String) obj[0];
            Long count = (Long) obj[1];
            result.put(optionId, count);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.NOT_SUPPORTED)
    public Long countVoteTotalByVoteThemeId(String voteThemeId) {
        if (voteThemeId == null || voteThemeId.trim().isEmpty()) {
            return 0L;
        }
        Query query = em.createQuery("select count(o) from VoteRecord o where o.voteThemeId=?1")
                .setParameter(1, voteThemeId);
        return (Long) query.getSingleResult();
    }

    @Override
    public void saveVoteTheme(VoteTheme voteTheme, List<VoteOption> voteOptionList) {
        this.save(voteTheme);
        if (voteOptionList != null && !voteOptionList.isEmpty()) {
            for (VoteOption option : voteOptionList) {
                this.save(option);
            }
        }
    }

    @Override
    public void updateVoteTheme(VoteTheme voteTheme, List<VoteOption> addVoteOptionList, List<VoteOption> editVoteOptionList, List<String> deleteVoteOptionIdList) {
        this.update(voteTheme);
        if (addVoteOptionList != null && !addVoteOptionList.isEmpty()) {
            for (VoteOption option : addVoteOptionList) {
                this.save(option);
            }
        }
        if (editVoteOptionList != null && !editVoteOptionList.isEmpty()) {
            for (VoteOption option : editVoteOptionList) {
                this.update(option);
            }
        }
        if (deleteVoteOptionIdList != null && !deleteVoteOptionIdList.isEmpty()) {
            for (String optionId : deleteVoteOptionIdList) {
                Query deleteRecord = em.createQuery("delete from VoteRecord o where o.voteOptionId=?1")
                        .setParameter(1, optionId);
                deleteRecord.executeUpdate();
                Query deleteOption = em.createQuery("delete from VoteOption o where o.id=?1")
                        .setParameter(1, optionId);
                deleteOption.executeUpdate();
            }
        }
    }

    @Override
    public void deleteVoteTheme(String voteThemeId) {
        if (voteThemeId == null || voteThemeId.trim().isEmpty()) {
            return;
        }
        Query deleteRecord = em.createQuery("delete from VoteRecord o where o.voteThemeId=?1")
                .setParameter(1, voteThemeId);
        deleteRecord.executeUpdate();
        Query deleteOption = em.createQuery("delete from VoteOption o where o.voteThemeId=?1")
                .setParameter(1, voteThemeId);
        deleteOption.executeUpdate();
        Query deleteTheme = em.createQuery("delete from VoteTheme o where o.id=?1")
                .setParameter(1, voteThemeId);
        deleteTheme.executeUpdate();
    }

    @Override
    public void addVoteRecord(VoteRecord voteRecord) {
        this.save(voteRecord);
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.NOT_SUPPORTED)
    public QueryResult<VoteTheme> findVoteThemePage(int firstIndex, int maxResult, String userName, Boolean isStaff) {
        QueryResult<VoteTheme> qr = new QueryResult<>();
        String jpql = "o.userName=?1 and o.isStaff=?2";
        List<Object> params = new ArrayList<>();
        params.add(userName);
        params.add(isStaff);

        LinkedHashMap<String, String> orderby = new LinkedHashMap<>();
        orderby.put("createDate", "desc");

        qr = this.getScrollData(VoteTheme.class, firstIndex, maxResult, jpql, params.toArray(), orderby);
        return qr;
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.NOT_SUPPORTED)
    public QueryResult<VoteTheme> findAllVoteThemePage(int firstIndex, int maxResult) {
        QueryResult<VoteTheme> qr = new QueryResult<>();
        LinkedHashMap<String, String> orderby = new LinkedHashMap<>();
        orderby.put("createDate", "desc");
        qr = this.getScrollData(VoteTheme.class, firstIndex, maxResult, null, null, orderby);
        return qr;
    }
}
