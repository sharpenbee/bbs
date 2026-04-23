package cms.repository.vote;

import cms.dto.QueryResult;
import cms.model.vote.VoteOption;
import cms.model.vote.VoteRecord;
import cms.model.vote.VoteTheme;
import cms.repository.besa.DAO;

import java.util.List;
import java.util.Map;

public interface VoteRepository extends DAO<VoteTheme> {

    VoteTheme findVoteThemeById(String voteThemeId);

    VoteTheme findVoteThemeBySourceParameterId(Integer module, String sourceParameterId);

    List<VoteOption> findVoteOptionByVoteThemeId(String voteThemeId);

    VoteOption findVoteOptionById(String voteOptionId);

    VoteRecord findVoteRecordByUserAndTheme(String userName, String voteThemeId);

    List<VoteRecord> findVoteRecordByVoteThemeId(String voteThemeId);

    Map<String, Long> countVoteByVoteThemeId(String voteThemeId);

    void saveVoteTheme(VoteTheme voteTheme, List<VoteOption> voteOptionList);

    void updateVoteTheme(VoteTheme voteTheme, List<VoteOption> addVoteOptionList, List<VoteOption> editVoteOptionList, List<String> deleteVoteOptionIdList);

    void deleteVoteTheme(String voteThemeId);

    void addVoteRecord(VoteRecord voteRecord);

    QueryResult<VoteTheme> findVoteThemePage(int firstIndex, int maxResult, String userName, Boolean isStaff);

    QueryResult<VoteTheme> findAllVoteThemePage(int firstIndex, int maxResult);

    Long countVoteTotalByVoteThemeId(String voteThemeId);
}
