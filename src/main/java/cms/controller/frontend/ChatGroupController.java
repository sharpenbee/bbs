package cms.controller.frontend;

import cms.annotation.DynamicRouteEnum;
import cms.annotation.DynamicRouteTarget;
import cms.annotation.RoleAnnotation;
import cms.component.fileSystem.FileComponent;
import cms.dto.ClientRequestResult;
import cms.dto.PageForm;
import cms.dto.PageView;
import cms.dto.user.ResourceEnum;
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
import java.util.List;
import java.util.Map;

@RestController
public class ChatGroupController {

    @Resource
    ChatGroupService chatGroupService;
    @Resource
    FileComponent fileComponent;

    @DynamicRouteTarget(route = DynamicRouteEnum.DEFAULT_1210100)
    @RequestMapping(value="/user/control/chatGroupList", method= RequestMethod.GET)
    public Map<String, Object> chatGroupList(PageForm pageForm, HttpServletRequest request){
        String fileServerAddress = fileComponent.fileServerAddress(request);
        return chatGroupService.getGroupList(pageForm.getPage(), fileServerAddress);
    }

    @DynamicRouteTarget(route = DynamicRouteEnum.DEFAULT_1210200)
    @RequestMapping(value="/user/control/chatGroupDetail", method=RequestMethod.GET)
    public Map<String, Object> chatGroupDetail(String groupId, HttpServletRequest request){
        String fileServerAddress = fileComponent.fileServerAddress(request);
        return chatGroupService.getGroupDetail(groupId, fileServerAddress);
    }

    @DynamicRouteTarget(route = DynamicRouteEnum.DEFAULT_1210300)
    @RequestMapping(value="/user/control/createChatGroup", method=RequestMethod.GET)
    public ClientRequestResult createChatGroupUI(){
        return ClientRequestResult.success();
    }

    @RoleAnnotation(resourceCode=ResourceEnum._9001000)
    @DynamicRouteTarget(route = DynamicRouteEnum.DEFAULT_1210400)
    @RequestMapping(value="/user/control/createChatGroup", method=RequestMethod.POST)
    public ClientRequestResult createChatGroup(String groupName, String description, 
                                                 String memberUserIds, HttpServletRequest request){
        String fileServerAddress = fileComponent.fileServerAddress(request);
        List<Long> memberIds = parseUserIds(memberUserIds);
        Map<String, Object> result = chatGroupService.createGroup(groupName, description, memberIds, fileServerAddress);
        return ClientRequestResult.success(result);
    }

    @RoleAnnotation(resourceCode=ResourceEnum._9003000)
    @DynamicRouteTarget(route = DynamicRouteEnum.DEFAULT_1210500)
    @RequestMapping(value="/user/control/addGroupMembers", method=RequestMethod.POST)
    public ClientRequestResult addGroupMembers(String groupId, String memberUserIds){
        List<Long> memberIds = parseUserIds(memberUserIds);
        chatGroupService.addMembers(groupId, memberIds);
        return ClientRequestResult.success();
    }

    @RoleAnnotation(resourceCode=ResourceEnum._9002000)
    @DynamicRouteTarget(route = DynamicRouteEnum.DEFAULT_1210600)
    @RequestMapping(value="/user/control/sendGroupMessage", method=RequestMethod.POST)
    public ClientRequestResult sendGroupMessage(String groupId, String content, HttpServletRequest request){
        String fileServerAddress = fileComponent.fileServerAddress(request);
        ChatGroupMessage message = chatGroupService.sendMessage(groupId, content, fileServerAddress);
        return ClientRequestResult.success(Map.of("message", message));
    }

    @DynamicRouteTarget(route = DynamicRouteEnum.DEFAULT_1210700)
    @RequestMapping(value="/user/control/groupMessageList", method=RequestMethod.GET)
    public Map<String, Object> groupMessageList(String groupId, PageForm pageForm, HttpServletRequest request){
        String fileServerAddress = fileComponent.fileServerAddress(request);
        return chatGroupService.getMessageList(groupId, pageForm.getPage(), fileServerAddress);
    }

    @DynamicRouteTarget(route = DynamicRouteEnum.DEFAULT_1210800)
    @RequestMapping(value="/user/control/groupMemberList", method=RequestMethod.GET)
    public Map<String, Object> groupMemberList(String groupId, PageForm pageForm, HttpServletRequest request){
        String fileServerAddress = fileComponent.fileServerAddress(request);
        return chatGroupService.getMemberList(groupId, pageForm.getPage(), fileServerAddress);
    }

    @DynamicRouteTarget(route = DynamicRouteEnum.DEFAULT_1210900)
    @RequestMapping(value="/user/control/quitGroup", method=RequestMethod.POST)
    public ClientRequestResult quitGroup(String groupId, Long userId){
        chatGroupService.removeMember(groupId, userId);
        return ClientRequestResult.success();
    }

    @RoleAnnotation(resourceCode=ResourceEnum._9004000)
    @DynamicRouteTarget(route = DynamicRouteEnum.DEFAULT_1211000)
    @RequestMapping(value="/user/control/dissolveGroup", method=RequestMethod.POST)
    public ClientRequestResult dissolveGroup(String groupId){
        chatGroupService.dissolveGroup(groupId);
        return ClientRequestResult.success();
    }

    @DynamicRouteTarget(route = DynamicRouteEnum.DEFAULT_1211100)
    @RequestMapping(value="/user/control/markMessageAsRead", method=RequestMethod.POST)
    public ClientRequestResult markMessageAsRead(String messageId){
        chatGroupService.markMessageAsRead(messageId);
        return ClientRequestResult.success();
    }

    @DynamicRouteTarget(route = DynamicRouteEnum.DEFAULT_1211200)
    @RequestMapping(value="/user/control/markAllGroupMessagesAsRead", method=RequestMethod.POST)
    public ClientRequestResult markAllGroupMessagesAsRead(String groupId){
        chatGroupService.markAllMessagesAsRead(groupId);
        return ClientRequestResult.success();
    }

    @DynamicRouteTarget(route = DynamicRouteEnum.DEFAULT_1211300)
    @RequestMapping(value="/user/control/groupUnreadCount", method=RequestMethod.GET)
    public Map<String, Object> groupUnreadCount(String groupId){
        Long count;
        if (groupId != null && !groupId.trim().isEmpty()) {
            count = chatGroupService.getUnreadCount(groupId);
        } else {
            count = chatGroupService.getAllUnreadCount();
        }
        Map<String, Object> result = new HashMap<>();
        result.put("unreadCount", count);
        return result;
    }

    private List<Long> parseUserIds(String userIdsStr) {
        if (userIdsStr == null || userIdsStr.trim().isEmpty()) {
            return null;
        }
        try {
            userIdsStr = userIdsStr.trim();
            if (userIdsStr.startsWith("[") && userIdsStr.endsWith("]")) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                return mapper.readValue(userIdsStr, new com.fasterxml.jackson.core.type.TypeReference<List<Long>>() {});
            } else {
                String[] ids = userIdsStr.split(",");
                List<Long> result = new java.util.ArrayList<>();
                for (String id : ids) {
                    String trimmed = id.trim();
                    if (!trimmed.isEmpty()) {
                        result.add(Long.parseLong(trimmed));
                    }
                }
                return result.isEmpty() ? null : result;
            }
        } catch (Exception e) {
            return null;
        }
    }
}
