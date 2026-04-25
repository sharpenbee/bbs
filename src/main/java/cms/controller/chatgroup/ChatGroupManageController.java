package cms.controller.chatgroup;

import cms.dto.PageForm;
import cms.dto.PageView;
import cms.dto.RequestResult;
import cms.dto.ResultCode;
import cms.model.chatgroup.ChatGroup;
import cms.model.chatgroup.ChatGroupMember;
import cms.model.chatgroup.ChatGroupMessage;
import cms.service.chatgroup.ChatGroupService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/control/chatgroup/manage")
public class ChatGroupManageController {

    @Resource
    ChatGroupService chatGroupService;

    @RequestMapping(params = "method=list", method = RequestMethod.GET)
    public RequestResult list(PageForm pageForm, Integer status) {
        PageView<ChatGroup> pageView = chatGroupService.getAdminGroupList(pageForm.getPage(), status);
        return new RequestResult(ResultCode.SUCCESS, pageView);
    }

    @RequestMapping(params = "method=show", method = RequestMethod.GET)
    public RequestResult show(String groupId) {
        ChatGroup chatGroup = chatGroupService.getAdminGroupDetail(groupId);
        if (chatGroup == null) {
            return new RequestResult(ResultCode.FAILURE, Map.of("error", "群聊不存在"));
        }
        return new RequestResult(ResultCode.SUCCESS, chatGroup);
    }

    @RequestMapping(params = "method=dissolve", method = RequestMethod.POST)
    public RequestResult dissolve(String groupId) {
        try {
            chatGroupService.adminDissolveGroup(groupId);
            return new RequestResult(ResultCode.SUCCESS, null);
        } catch (Exception e) {
            return new RequestResult(ResultCode.FAILURE, Map.of("error", e.getMessage()));
        }
    }

    @RequestMapping(params = "method=delete", method = RequestMethod.POST)
    public RequestResult delete(String groupId) {
        try {
            chatGroupService.adminDeleteGroup(groupId);
            return new RequestResult(ResultCode.SUCCESS, null);
        } catch (Exception e) {
            return new RequestResult(ResultCode.FAILURE, Map.of("error", e.getMessage()));
        }
    }

    @RequestMapping(params = "method=memberList", method = RequestMethod.GET)
    public RequestResult memberList(String groupId, PageForm pageForm, HttpServletRequest request) {
        String fileServerAddress = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/";
        Map<String, Object> result = chatGroupService.getMemberList(groupId, pageForm.getPage(), fileServerAddress);
        return new RequestResult(ResultCode.SUCCESS, result);
    }

    @RequestMapping(params = "method=messageList", method = RequestMethod.GET)
    public RequestResult messageList(String groupId, PageForm pageForm, HttpServletRequest request) {
        String fileServerAddress = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/";
        Map<String, Object> result = chatGroupService.getMessageList(groupId, pageForm.getPage(), fileServerAddress);
        return new RequestResult(ResultCode.SUCCESS, result);
    }

    @RequestMapping(params = "method=statistics", method = RequestMethod.GET)
    public RequestResult statistics() {
        Map<String, Object> result = new HashMap<>();
        result.put("totalGroups", 0L);
        result.put("activeGroups", 0L);
        result.put("dissolvedGroups", 0L);
        return new RequestResult(ResultCode.SUCCESS, result);
    }
}
