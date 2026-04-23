package cms.service.vote.impl;

import cms.component.staff.StaffCacheManager;
import cms.component.user.UserCacheManager;
import cms.config.BusinessException;
import cms.dto.PageView;
import cms.dto.QueryResult;
import cms.model.staff.SysUsers;
import cms.model.user.User;
import cms.model.vote.VoteOption;
import cms.model.vote.VoteRecord;
import cms.model.vote.VoteTheme;
import cms.repository.setting.SettingRepository;
import cms.repository.vote.VoteRepository;
import cms.service.vote.VoteService;
import cms.utils.UUIDUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class VoteServiceImpl implements VoteService {

    @Resource
    VoteRepository voteRepository;

    @Resource
    SettingRepository settingRepository;

    @Resource
    UserCacheManager userCacheManager;

    @Resource
    StaffCacheManager staffCacheManager;

    private static final int MAX_OPTIONS = 10;

    @Override
    public VoteTheme getVoteThemeById(String voteThemeId) {
        return voteRepository.findVoteThemeById(voteThemeId);
    }

    @Override
    public VoteTheme getVoteThemeBySourceParameterId(Integer module, String sourceParameterId) {
        return voteRepository.findVoteThemeBySourceParameterId(module, sourceParameterId);
    }

    @Override
    public List<VoteOption> getVoteOptionsByThemeId(String voteThemeId) {
        return voteRepository.findVoteOptionByVoteThemeId(voteThemeId);
    }

    @Override
    public VoteOption getVoteOptionById(String voteOptionId) {
        return voteRepository.findVoteOptionById(voteOptionId);
    }

    @Override
    public VoteRecord getUserVoteRecord(String userName, String voteThemeId) {
        return voteRepository.findVoteRecordByUserAndTheme(userName, voteThemeId);
    }

    @Override
    public Map<String, Object> getVoteResult(String voteThemeId) {
        Map<String, Object> result = new LinkedHashMap<>();

        VoteTheme voteTheme = voteRepository.findVoteThemeById(voteThemeId);
        if (voteTheme == null) {
            throw new BusinessException(Map.of("voteThemeId", "投票不存在"));
        }

        List<VoteOption> optionList = voteRepository.findVoteOptionByVoteThemeId(voteThemeId);
        Map<String, Long> voteCountMap = voteRepository.countVoteByVoteThemeId(voteThemeId);
        Long totalVotes = voteRepository.countVoteTotalByVoteThemeId(voteThemeId);

        List<Map<String, Object>> optionResults = new ArrayList<>();
        for (VoteOption option : optionList) {
            Map<String, Object> optionResult = new LinkedHashMap<>();
            optionResult.put("id", option.getId());
            optionResult.put("text", option.getText());
            optionResult.put("sort", option.getSort());

            Long count = voteCountMap.getOrDefault(option.getId(), 0L);
            optionResult.put("count", count);

            double percentage = 0.0;
            if (totalVotes > 0) {
                percentage = (count * 100.0) / totalVotes;
                percentage = Math.round(percentage * 100.0) / 100.0;
            }
            optionResult.put("percentage", percentage);

            optionResults.add(optionResult);
        }

        result.put("voteTheme", voteTheme);
        result.put("optionResults", optionResults);
        result.put("totalVotes", totalVotes);
        result.put("status", getVoteStatus(voteTheme));

        return result;
    }

    @Override
    public void saveVoteTheme(VoteTheme voteTheme, List<VoteOption> voteOptionList) {
        if (voteOptionList == null || voteOptionList.isEmpty()) {
            throw new BusinessException(Map.of("voteOptionList", "投票选项不能为空"));
        }
        if (voteOptionList.size() > MAX_OPTIONS) {
            throw new BusinessException(Map.of("voteOptionList", "投票选项最多只能有" + MAX_OPTIONS + "个"));
        }

        voteRepository.saveVoteTheme(voteTheme, voteOptionList);
    }

    @Override
    public void updateVoteTheme(VoteTheme voteTheme, List<VoteOption> addVoteOptionList, List<VoteOption> editVoteOptionList, List<String> deleteVoteOptionIdList) {
        voteRepository.updateVoteTheme(voteTheme, addVoteOptionList, editVoteOptionList, deleteVoteOptionIdList);
    }

    @Override
    public void deleteVoteTheme(String voteThemeId) {
        voteRepository.deleteVoteTheme(voteThemeId);
    }

    @Override
    public void addVoteRecord(String voteOptionId, String userName, String ip, Integer module, String sourceParameterId) {
        if (voteOptionId == null || voteOptionId.trim().isEmpty()) {
            throw new BusinessException(Map.of("voteOptionId", "投票选项不能为空"));
        }

        VoteOption voteOption = voteRepository.findVoteOptionById(voteOptionId);
        if (voteOption == null) {
            throw new BusinessException(Map.of("voteOptionId", "投票选项不存在"));
        }

        String voteThemeId = voteOption.getVoteThemeId();
        VoteTheme voteTheme = voteRepository.findVoteThemeById(voteThemeId);
        if (voteTheme == null) {
            throw new BusinessException(Map.of("voteThemeId", "投票主题不存在"));
        }

        int status = getVoteStatus(voteTheme);
        if (status != 20) {
            throw new BusinessException(Map.of("vote", "投票当前不可用"));
        }

        if (hasUserVoted(userName, voteThemeId)) {
            throw new BusinessException(Map.of("vote", "您已经投过票了"));
        }

        VoteRecord voteRecord = new VoteRecord();
        String recordId = userName + "_" + voteThemeId + "_" + voteOptionId;
        voteRecord.setId(recordId);
        voteRecord.setVoteThemeId(voteThemeId);
        voteRecord.setVoteOptionId(voteOptionId);
        voteRecord.setUserName(userName);
        voteRecord.setIp(ip);
        voteRecord.setVoteTime(LocalDateTime.now());
        voteRecord.setModule(module);
        voteRecord.setSourceParameterId(sourceParameterId);

        voteRepository.addVoteRecord(voteRecord);
    }

    @Override
    public Map<String, Object> getUserVoteList(int page, String userName, Boolean isStaff, String fileServerAddress) {
        if (userName == null || userName.trim().isEmpty()) {
            throw new BusinessException(Map.of("userName", "用户名称不能为空"));
        }

        Map<String, Object> returnValue = new LinkedHashMap<>();

        PageView<VoteTheme> pageView = new PageView<>(settingRepository.findSystemSetting_cache().getBackstagePageNumber(), page, 10);
        int firstIndex = (page - 1) * pageView.getMaxresult();

        QueryResult<VoteTheme> qr = voteRepository.findVoteThemePage(firstIndex, pageView.getMaxresult(), userName, isStaff);

        if (qr != null && qr.getResultlist() != null && !qr.getResultlist().isEmpty()) {
            for (VoteTheme voteTheme : qr.getResultlist()) {
                List<VoteOption> optionList = voteRepository.findVoteOptionByVoteThemeId(voteTheme.getId());
                voteTheme.setVoteOptionList(optionList);
                voteTheme.setStatus(getVoteStatus(voteTheme));

                if (isStaff) {
                    SysUsers sysUsers = staffCacheManager.query_cache_findByUserAccount(voteTheme.getUserName());
                    if (sysUsers != null) {
                        voteTheme.setNickname(sysUsers.getNickname());
                        if (sysUsers.getAvatarName() != null && !sysUsers.getAvatarName().trim().isEmpty()) {
                            voteTheme.setAvatarPath(fileServerAddress + sysUsers.getAvatarPath());
                            voteTheme.setAvatarName(sysUsers.getAvatarName());
                        }
                    }
                    voteTheme.setAccount(voteTheme.getUserName());
                } else {
                    User user = userCacheManager.query_cache_findUserByUserName(voteTheme.getUserName());
                    if (user != null) {
                        voteTheme.setUserId(user.getId());
                        voteTheme.setAccount(user.getAccount());
                        voteTheme.setNickname(user.getNickname());
                        if (user.getAvatarName() != null && !user.getAvatarName().trim().isEmpty()) {
                            voteTheme.setAvatarPath(fileServerAddress + user.getAvatarPath());
                            voteTheme.setAvatarName(user.getAvatarName());
                        }
                    }
                }
            }
        }

        pageView.setQueryResult(qr);
        returnValue.put("pageView", pageView);

        return returnValue;
    }

    @Override
    public Map<String, Object> getAllVoteList(int page, String fileServerAddress) {
        Map<String, Object> returnValue = new LinkedHashMap<>();

        PageView<VoteTheme> pageView = new PageView<>(settingRepository.findSystemSetting_cache().getBackstagePageNumber(), page, 10);
        int firstIndex = (page - 1) * pageView.getMaxresult();

        QueryResult<VoteTheme> qr = voteRepository.findAllVoteThemePage(firstIndex, pageView.getMaxresult());

        if (qr != null && qr.getResultlist() != null && !qr.getResultlist().isEmpty()) {
            for (VoteTheme voteTheme : qr.getResultlist()) {
                List<VoteOption> optionList = voteRepository.findVoteOptionByVoteThemeId(voteTheme.getId());
                voteTheme.setVoteOptionList(optionList);
                voteTheme.setStatus(getVoteStatus(voteTheme));

                if (voteTheme.getIsStaff()) {
                    SysUsers sysUsers = staffCacheManager.query_cache_findByUserAccount(voteTheme.getUserName());
                    if (sysUsers != null) {
                        voteTheme.setNickname(sysUsers.getNickname());
                        if (sysUsers.getAvatarName() != null && !sysUsers.getAvatarName().trim().isEmpty()) {
                            voteTheme.setAvatarPath(fileServerAddress + sysUsers.getAvatarPath());
                            voteTheme.setAvatarName(sysUsers.getAvatarName());
                        }
                    }
                    voteTheme.setAccount(voteTheme.getUserName());
                } else {
                    User user = userCacheManager.query_cache_findUserByUserName(voteTheme.getUserName());
                    if (user != null) {
                        voteTheme.setUserId(user.getId());
                        voteTheme.setAccount(user.getAccount());
                        voteTheme.setNickname(user.getNickname());
                        if (user.getAvatarName() != null && !user.getAvatarName().trim().isEmpty()) {
                            voteTheme.setAvatarPath(fileServerAddress + user.getAvatarPath());
                            voteTheme.setAvatarName(user.getAvatarName());
                        }
                    }
                }
            }
        }

        pageView.setQueryResult(qr);
        returnValue.put("pageView", pageView);

        return returnValue;
    }

    @Override
    public boolean hasUserVoted(String userName, String voteThemeId) {
        VoteRecord record = voteRepository.findVoteRecordByUserAndTheme(userName, voteThemeId);
        return record != null;
    }

    @Override
    public int getVoteStatus(VoteTheme voteTheme) {
        if (voteTheme == null) {
            return 10;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime createDate = voteTheme.getCreateDate();
        LocalDateTime endDate = voteTheme.getEndDate();

        if (endDate != null && now.isAfter(endDate)) {
            return 30;
        }

        if (createDate != null && now.isBefore(createDate)) {
            return 10;
        }

        return 20;
    }
}
