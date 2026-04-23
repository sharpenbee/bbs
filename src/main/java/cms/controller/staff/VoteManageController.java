package cms.controller.staff;


import cms.component.fileSystem.FileComponent;
import cms.dto.PageForm;
import cms.dto.RequestResult;
import cms.dto.ResultCode;
import cms.model.vote.VoteOption;
import cms.model.vote.VoteTheme;
import cms.service.vote.VoteService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


/**
 * 投票管理控制器
 *
 */
@RestController
@RequestMapping("/control/vote/manage")
public class VoteManageController {
    @Resource
    VoteService voteService;
    @Resource
    FileComponent fileComponent;

    /**
     * 投票管理 列表页面显示
     * @param pageForm 分页
     * @param request 请求信息
     * @return
     */
    @RequestMapping(params = "method=voteList", method = RequestMethod.GET)
    public RequestResult voteList(PageForm pageForm, HttpServletRequest request){
        String fileServerAddress = fileComponent.fileServerAddress(request);
        Map<String,Object> returnValue = voteService.getAllVoteList(pageForm.getPage(), fileServerAddress);
        return new RequestResult(ResultCode.SUCCESS, returnValue);
    }

    /**
     * 投票管理 投票详情
     * @param voteThemeId 投票主题Id
     * @return
     */
    @RequestMapping(params = "method=voteDetail", method = RequestMethod.GET)
    public RequestResult voteDetail(String voteThemeId){
        if(voteThemeId == null || voteThemeId.trim().isEmpty()){
            return new RequestResult(ResultCode.FAILURE, Map.of("voteThemeId", "投票主题Id不能为空"));
        }

        VoteTheme voteTheme = voteService.getVoteThemeById(voteThemeId);
        if(voteTheme == null){
            return new RequestResult(ResultCode.FAILURE, Map.of("voteThemeId", "投票不存在"));
        }

        List<VoteOption> optionList = voteService.getVoteOptionsByThemeId(voteThemeId);
        voteTheme.setVoteOptionList(optionList);
        voteTheme.setStatus(voteService.getVoteStatus(voteTheme));

        Map<String, Object> voteResult = voteService.getVoteResult(voteThemeId);

        return new RequestResult(ResultCode.SUCCESS, Map.of("voteTheme", voteTheme, "voteResult", voteResult));
    }

    /**
     * 投票管理 删除投票
     * @param voteThemeId 投票主题Id
     * @return
     */
    @RequestMapping(params = "method=deleteVote", method = RequestMethod.POST)
    public RequestResult deleteVote(String voteThemeId){
        if(voteThemeId == null || voteThemeId.trim().isEmpty()){
            return new RequestResult(ResultCode.FAILURE, Map.of("voteThemeId", "投票主题Id不能为空"));
        }

        VoteTheme voteTheme = voteService.getVoteThemeById(voteThemeId);
        if(voteTheme == null){
            return new RequestResult(ResultCode.FAILURE, Map.of("voteThemeId", "投票不存在"));
        }

        voteService.deleteVoteTheme(voteThemeId);

        return new RequestResult(ResultCode.SUCCESS, null);
    }
}
