package cms.service.vote;

import cms.model.vote.VoteOption;
import cms.model.vote.VoteRecord;
import cms.model.vote.VoteTheme;

import java.util.List;
import java.util.Map;

public interface VoteService {

    VoteTheme getVoteThemeById(String voteThemeId);

    VoteTheme getVoteThemeBySourceParameterId(Integer module, String sourceParameterId);

    List<VoteOption> getVoteOptionsByThemeId(String voteThemeId);

    VoteOption getVoteOptionById(String voteOptionId);

    VoteRecord getUserVoteRecord(String userName, String voteThemeId);

    Map<String, Object> getVoteResult(String voteThemeId);

    void saveVoteTheme(VoteTheme voteTheme, List<VoteOption> voteOptionList);

    void updateVoteTheme(VoteTheme voteTheme, List<VoteOption> addVoteOptionList, List<VoteOption> editVoteOptionList, List<String> deleteVoteOptionIdList);

    void deleteVoteTheme(String voteThemeId);

    void addVoteRecord(String voteOptionId, String userName, String ip, Integer module, String sourceParameterId);

    Map<String, Object> getUserVoteList(int page, String userName, Boolean isStaff, String fileServerAddress);

    Map<String, Object> getAllVoteList(int page, String fileServerAddress);

    boolean hasUserVoted(String userName, String voteThemeId);

    int getVoteStatus(VoteTheme voteTheme);
}
