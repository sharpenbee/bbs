package cms.controller.frontend;

import cms.annotation.DynamicRouteEnum;
import cms.annotation.DynamicRouteTarget;
import cms.annotation.RoleAnnotation;
import cms.component.fileSystem.FileComponent;
import cms.config.BusinessException;
import cms.dto.PageForm;
import cms.dto.RequestResult;
import cms.dto.ResultCode;
import cms.dto.user.AccessUser;
import cms.dto.user.ResourceEnum;
import cms.model.vote.VoteOption;
import cms.model.vote.VoteTheme;
import cms.service.vote.VoteService;
import cms.utils.AccessUserThreadLocal;
import cms.utils.IpAddress;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController("frontendVoteController")
public class VoteController {

    @Resource
    VoteService voteService;

    @Resource
    FileComponent fileComponent;

    @DynamicRouteTarget(route = DynamicRouteEnum.CUSTOM_6140100)
    public VoteTheme voteThemeDetail(String voteThemeId) {
        if (voteThemeId == null || voteThemeId.trim().isEmpty()) {
            throw new BusinessException(Map.of("voteThemeId", "投票主题Id不能为空"));
        }
        VoteTheme voteTheme = voteService.getVoteThemeById(voteThemeId);
        if (voteTheme != null) {
            List<VoteOption> optionList = voteService.getVoteOptionsByThemeId(voteThemeId);
            voteTheme.setVoteOptionList(optionList);
            voteTheme.setStatus(voteService.getVoteStatus(voteTheme));
        }
        return voteTheme;
    }

    @RequestMapping(value = "/control/vote/detail", method = RequestMethod.GET)
    public RequestResult getVoteDetail(String voteThemeId, HttpServletRequest request) {
        if (voteThemeId == null || voteThemeId.trim().isEmpty()) {
            throw new BusinessException(Map.of("voteThemeId", "投票主题Id不能为空"));
        }

        Map<String, Object> returnValue = new HashMap<>();

        VoteTheme voteTheme = voteService.getVoteThemeById(voteThemeId);
        if (voteTheme == null) {
            throw new BusinessException(Map.of("voteThemeId", "投票不存在"));
        }

        List<VoteOption> optionList = voteService.getVoteOptionsByThemeId(voteThemeId);
        voteTheme.setVoteOptionList(optionList);
        voteTheme.setStatus(voteService.getVoteStatus(voteTheme));

        Map<String, Object> voteResult = voteService.getVoteResult(voteThemeId);
        returnValue.put("voteResult", voteResult);

        if (voteResult != null && optionList != null) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> optionResults = (List<Map<String, Object>>) voteResult.get("optionResults");
            if (optionResults != null) {
                Map<String, Long> voteCountMap = new HashMap<>();
                for (Map<String, Object> optionResult : optionResults) {
                    String optionId = (String) optionResult.get("id");
                    Long count = ((Number) optionResult.get("count")).longValue();
                    voteCountMap.put(optionId, count);
                }
                for (VoteOption option : optionList) {
                    Long count = voteCountMap.getOrDefault(option.getId(), 0L);
                    option.setTotalVotes(count);
                }
            }
        }

        returnValue.put("voteTheme", voteTheme);

        AccessUser accessUser = AccessUserThreadLocal.get();
        if (accessUser != null) {
            boolean hasVoted = voteService.hasUserVoted(accessUser.getUserName(), voteThemeId);
            returnValue.put("hasVoted", hasVoted);
        } else {
            returnValue.put("hasVoted", false);
        }

        return new RequestResult(ResultCode.SUCCESS, returnValue);
    }

    @RequestMapping(value = "/control/vote/result", method = RequestMethod.GET)
    public RequestResult getVoteResult(String voteThemeId, HttpServletRequest request) {
        if (voteThemeId == null || voteThemeId.trim().isEmpty()) {
            throw new BusinessException(Map.of("voteThemeId", "投票主题Id不能为空"));
        }

        Map<String, Object> voteResult = voteService.getVoteResult(voteThemeId);
        return new RequestResult(ResultCode.SUCCESS, voteResult);
    }

    @RoleAnnotation(resourceCode = ResourceEnum._8001000)
    @DynamicRouteTarget(route = DynamicRouteEnum.DEFAULT_1140100)
    @RequestMapping(value = "/user/control/vote/addVoteRecord", method = RequestMethod.POST)
    public Map<String, Object> add(String voteOptionId,
                                  HttpServletRequest request) {
        if (voteOptionId == null || voteOptionId.trim().isEmpty()) {
            throw new BusinessException(Map.of("voteOptionId", "投票选项Id不能为空"));
        }

        AccessUser accessUser = AccessUserThreadLocal.get();
        if (accessUser == null) {
            throw new BusinessException(Map.of("user", "用户未登录"));
        }

        VoteOption voteOption = voteService.getVoteOptionById(voteOptionId);
        if (voteOption == null) {
            throw new BusinessException(Map.of("voteOptionId", "投票选项不存在"));
        }

        String ip = IpAddress.getClientIpAddress(request);
        voteService.addVoteRecord(voteOptionId, accessUser.getUserName(), ip,
                voteOption.getModule(), voteOption.getSourceParameterId());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "投票成功");

        return result;
    }

    @RoleAnnotation(resourceCode = ResourceEnum._8001000)
    @RequestMapping(value = "/user/control/vote/submit", method = RequestMethod.POST)
    public RequestResult submitVote(String voteOptionId, HttpServletRequest request) {
        if (voteOptionId == null || voteOptionId.trim().isEmpty()) {
            throw new BusinessException(Map.of("voteOptionId", "投票选项Id不能为空"));
        }

        AccessUser accessUser = AccessUserThreadLocal.get();
        if (accessUser == null) {
            throw new BusinessException(Map.of("user", "用户未登录"));
        }

        VoteOption voteOption = voteService.getVoteOptionById(voteOptionId);
        if (voteOption == null) {
            throw new BusinessException(Map.of("voteOptionId", "投票选项不存在"));
        }

        String ip = IpAddress.getClientIpAddress(request);
        voteService.addVoteRecord(voteOptionId, accessUser.getUserName(), ip,
                voteOption.getModule(), voteOption.getSourceParameterId());

        Map<String, Object> voteResult = voteService.getVoteResult(voteOption.getVoteThemeId());

        return new RequestResult(ResultCode.SUCCESS, voteResult);
    }

    @RoleAnnotation(resourceCode = ResourceEnum._8001000)
    @RequestMapping(value = "/user/control/vote/myList", method = RequestMethod.GET)
    public RequestResult getMyVoteList(PageForm pageForm, HttpServletRequest request) {
        AccessUser accessUser = AccessUserThreadLocal.get();
        if (accessUser == null) {
            throw new BusinessException(Map.of("user", "用户未登录"));
        }

        String fileServerAddress = fileComponent.fileServerAddress(request);
        Map<String, Object> returnValue = voteService.getUserVoteList(
                pageForm.getPage(), accessUser.getUserName(), false, fileServerAddress);

        return new RequestResult(ResultCode.SUCCESS, returnValue);
    }
}
