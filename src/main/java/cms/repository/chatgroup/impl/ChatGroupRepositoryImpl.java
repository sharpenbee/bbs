package cms.repository.chatgroup.impl;

import cms.dto.QueryResult;
import cms.model.chatgroup.ChatGroup;
import cms.model.chatgroup.ChatGroupMember;
import cms.model.chatgroup.ChatGroupMessage;
import cms.model.chatgroup.MessageReadStatus;
import cms.repository.besa.DaoSupport;
import cms.repository.chatgroup.ChatGroupRepository;
import jakarta.persistence.Query;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Repository
@Transactional
public class ChatGroupRepositoryImpl extends DaoSupport<ChatGroup> implements ChatGroupRepository {
	private static final Logger logger = LogManager.getLogger(ChatGroupRepositoryImpl.class);

	@Override
	@Transactional(readOnly=true, propagation=Propagation.NOT_SUPPORTED)
	public ChatGroup findById(String groupId) {
		Query query = em.createQuery("select o from ChatGroup o where o.id=?1")
				.setParameter(1, groupId);
		List<ChatGroup> list = query.getResultList();
		return list.isEmpty() ? null : list.get(0);
	}

	@Override
	@Transactional(readOnly=true, propagation=Propagation.NOT_SUPPORTED)
	public QueryResult<ChatGroup> findChatGroupList(int firstIndex, int maxResult) {
		QueryResult<ChatGroup> qr = new QueryResult<>();
		Query query = em.createQuery("select o from ChatGroup o order by o.createTimeFormat desc");
		query.setFirstResult(firstIndex);
		query.setMaxResults(maxResult);
		qr.setResultlist(query.getResultList());
		
		query = em.createQuery("select count(o) from ChatGroup o");
		qr.setTotalrecord((Long)query.getSingleResult());
		return qr;
	}

	@Override
	@Transactional(readOnly=true, propagation=Propagation.NOT_SUPPORTED)
	public QueryResult<ChatGroup> findChatGroupListByStatus(Integer status, int firstIndex, int maxResult) {
		QueryResult<ChatGroup> qr = new QueryResult<>();
		Query query = em.createQuery("select o from ChatGroup o where o.status=?1 order by o.createTimeFormat desc")
				.setParameter(1, status);
		query.setFirstResult(firstIndex);
		query.setMaxResults(maxResult);
		qr.setResultlist(query.getResultList());
		
		query = em.createQuery("select count(o) from ChatGroup o where o.status=?1")
				.setParameter(1, status);
		qr.setTotalrecord((Long)query.getSingleResult());
		return qr;
	}

	@Override
	@Transactional(readOnly=true, propagation=Propagation.NOT_SUPPORTED)
	public QueryResult<ChatGroup> findUserChatGroups(Long userId, int firstIndex, int maxResult) {
		QueryResult<ChatGroup> qr = new QueryResult<>();
		Query query = em.createQuery(
				"select g from ChatGroup g, ChatGroupMember m " +
				"where g.id = m.groupId and m.userId=?1 and m.status=10 and g.status=10 " +
				"order by g.createTimeFormat desc")
				.setParameter(1, userId);
		query.setFirstResult(firstIndex);
		query.setMaxResults(maxResult);
		qr.setResultlist(query.getResultList());
		
		query = em.createQuery(
				"select count(g) from ChatGroup g, ChatGroupMember m " +
				"where g.id = m.groupId and m.userId=?1 and m.status=10 and g.status=10")
				.setParameter(1, userId);
		qr.setTotalrecord((Long)query.getSingleResult());
		return qr;
	}

	@Override
	public void saveChatGroup(ChatGroup chatGroup) {
		chatGroup.setCreateTimeFormat(Instant.now().toEpochMilli());
		this.save(chatGroup);
	}

	@Override
	public void updateChatGroup(ChatGroup chatGroup) {
		this.update(chatGroup);
	}

	@Override
	public void deleteChatGroup(String groupId) {
		this.delete(ChatGroup.class, groupId);
	}

	@Override
	public void dissolveChatGroup(String groupId) {
		Query query = em.createQuery("update ChatGroup o set o.status=110 where o.id=?1")
				.setParameter(1, groupId);
		query.executeUpdate();
	}

	@Override
	@Transactional(readOnly=true, propagation=Propagation.NOT_SUPPORTED)
	public ChatGroupMember findGroupMember(String groupId, Long userId) {
		Query query = em.createQuery(
				"select o from ChatGroupMember o where o.groupId=?1 and o.userId=?2")
				.setParameter(1, groupId)
				.setParameter(2, userId);
		List<ChatGroupMember> list = query.getResultList();
		return list.isEmpty() ? null : list.get(0);
	}

	@Override
	@Transactional(readOnly=true, propagation=Propagation.NOT_SUPPORTED)
	public List<ChatGroupMember> findGroupMembers(String groupId) {
		Query query = em.createQuery(
				"select o from ChatGroupMember o where o.groupId=?1 and o.status=10 order by o.joinTimeFormat desc")
				.setParameter(1, groupId);
		return query.getResultList();
	}

	@Override
	@Transactional(readOnly=true, propagation=Propagation.NOT_SUPPORTED)
	public QueryResult<ChatGroupMember> findGroupMembersPage(String groupId, int firstIndex, int maxResult) {
		QueryResult<ChatGroupMember> qr = new QueryResult<>();
		Query query = em.createQuery(
				"select o from ChatGroupMember o where o.groupId=?1 and o.status=10 order by o.joinTimeFormat desc")
				.setParameter(1, groupId);
		query.setFirstResult(firstIndex);
		query.setMaxResults(maxResult);
		qr.setResultlist(query.getResultList());
		
		query = em.createQuery(
				"select count(o) from ChatGroupMember o where o.groupId=?1 and o.status=10")
				.setParameter(1, groupId);
		qr.setTotalrecord((Long)query.getSingleResult());
		return qr;
	}

	@Override
	public void saveGroupMember(ChatGroupMember member) {
		member.setJoinTimeFormat(Instant.now().toEpochMilli());
		this.save(member);
	}

	@Override
	public void deleteGroupMember(String groupId, Long userId) {
		Query query = em.createQuery(
				"update ChatGroupMember o set o.status=110 where o.groupId=?1 and o.userId=?2")
				.setParameter(1, groupId)
				.setParameter(2, userId);
		query.executeUpdate();
	}

	@Override
	public void updateGroupMember(ChatGroupMember member) {
		this.update(member);
	}

	@Override
	@Transactional(readOnly=true, propagation=Propagation.NOT_SUPPORTED)
	public Long countGroupMembers(String groupId) {
		Query query = em.createQuery(
				"select count(o) from ChatGroupMember o where o.groupId=?1 and o.status=10")
				.setParameter(1, groupId);
		return (Long)query.getSingleResult();
	}

	@Override
	@Transactional(readOnly=true, propagation=Propagation.NOT_SUPPORTED)
	public ChatGroupMessage findMessageById(String messageId) {
		Query query = em.createQuery("select o from ChatGroupMessage o where o.id=?1")
				.setParameter(1, messageId);
		List<ChatGroupMessage> list = query.getResultList();
		return list.isEmpty() ? null : list.get(0);
	}

	@Override
	@Transactional(readOnly=true, propagation=Propagation.NOT_SUPPORTED)
	public QueryResult<ChatGroupMessage> findGroupMessages(String groupId, int firstIndex, int maxResult) {
		QueryResult<ChatGroupMessage> qr = new QueryResult<>();
		Query query = em.createQuery(
				"select o from ChatGroupMessage o where o.groupId=?1 and o.status=10 order by o.sendTimeFormat desc")
				.setParameter(1, groupId);
		query.setFirstResult(firstIndex);
		query.setMaxResults(maxResult);
		qr.setResultlist(query.getResultList());
		
		query = em.createQuery(
				"select count(o) from ChatGroupMessage o where o.groupId=?1 and o.status=10")
				.setParameter(1, groupId);
		qr.setTotalrecord((Long)query.getSingleResult());
		return qr;
	}

	@Override
	@Transactional(readOnly=true, propagation=Propagation.NOT_SUPPORTED)
	public QueryResult<ChatGroupMessage> findGroupMessagesAfterTime(String groupId, Long timeFormat, int firstIndex, int maxResult) {
		QueryResult<ChatGroupMessage> qr = new QueryResult<>();
		Query query = em.createQuery(
				"select o from ChatGroupMessage o where o.groupId=?1 and o.sendTimeFormat>?2 and o.status=10 order by o.sendTimeFormat asc")
				.setParameter(1, groupId)
				.setParameter(2, timeFormat);
		query.setFirstResult(firstIndex);
		query.setMaxResults(maxResult);
		qr.setResultlist(query.getResultList());
		
		query = em.createQuery(
				"select count(o) from ChatGroupMessage o where o.groupId=?1 and o.sendTimeFormat>?2 and o.status=10")
				.setParameter(1, groupId)
				.setParameter(2, timeFormat);
		qr.setTotalrecord((Long)query.getSingleResult());
		return qr;
	}

	@Override
	public void saveMessage(ChatGroupMessage message) {
		message.setSendTimeFormat(Instant.now().toEpochMilli());
		this.save(message);
	}

	@Override
	public void deleteMessage(String messageId) {
		Query query = em.createQuery("update ChatGroupMessage o set o.status=110 where o.id=?1")
				.setParameter(1, messageId);
		query.executeUpdate();
	}

	@Override
	@Transactional(readOnly=true, propagation=Propagation.NOT_SUPPORTED)
	public Long countGroupMessages(String groupId) {
		Query query = em.createQuery(
				"select count(o) from ChatGroupMessage o where o.groupId=?1 and o.status=10")
				.setParameter(1, groupId);
		return (Long)query.getSingleResult();
	}

	@Override
	@Transactional(readOnly=true, propagation=Propagation.NOT_SUPPORTED)
	public MessageReadStatus findReadStatus(String messageId, Long userId) {
		Query query = em.createQuery(
				"select o from MessageReadStatus o where o.messageId=?1 and o.userId=?2")
				.setParameter(1, messageId)
				.setParameter(2, userId);
		List<MessageReadStatus> list = query.getResultList();
		return list.isEmpty() ? null : list.get(0);
	}

	@Override
	public void saveReadStatus(MessageReadStatus readStatus) {
		readStatus.setReadTimeFormat(Instant.now().toEpochMilli());
		this.save(readStatus);
	}

	@Override
	public void markMessageAsRead(String messageId, Long userId) {
		if (findReadStatus(messageId, userId) == null) {
			MessageReadStatus status = new MessageReadStatus();
			status.setId(java.util.UUID.randomUUID().toString());
			status.setMessageId(messageId);
			status.setUserId(userId);
			saveReadStatus(status);
		}
	}

	@Override
	public void markAllMessagesAsRead(String groupId, Long userId) {
		Query query = em.createQuery(
				"select o.id from ChatGroupMessage o where o.groupId=?1 and o.status=10")
				.setParameter(1, groupId);
		List<String> messageIds = query.getResultList();
		
		for (String messageId : messageIds) {
			markMessageAsRead(messageId, userId);
		}
	}

	@Override
	@Transactional(readOnly=true, propagation=Propagation.NOT_SUPPORTED)
	public Long countUnreadMessages(String groupId, Long userId) {
		Query query = em.createQuery(
				"select count(m) from ChatGroupMessage m " +
				"where m.groupId=?1 and m.status=10 and m.id not in (" +
				"  select r.messageId from MessageReadStatus r where r.userId=?2" +
				")")
				.setParameter(1, groupId)
				.setParameter(2, userId);
		return (Long)query.getSingleResult();
	}

	@Override
	@Transactional(readOnly=true, propagation=Propagation.NOT_SUPPORTED)
	public Long countAllUnreadMessages(Long userId) {
		Query query = em.createQuery(
				"select count(m) from ChatGroupMessage m, ChatGroupMember mem " +
				"where m.groupId = mem.groupId and mem.userId=?1 and mem.status=10 " +
				"and m.status=10 and m.id not in (" +
				"  select r.messageId from MessageReadStatus r where r.userId=?1" +
				")")
				.setParameter(1, userId);
		return (Long)query.getSingleResult();
	}

	@Override
	@Transactional(readOnly=true, propagation=Propagation.NOT_SUPPORTED)
	public ChatGroupMessage findLastMessage(String groupId) {
		Query query = em.createQuery(
				"select o from ChatGroupMessage o where o.groupId=?1 and o.status=10 order by o.sendTimeFormat desc")
				.setParameter(1, groupId)
				.setMaxResults(1);
		List<ChatGroupMessage> list = query.getResultList();
		return list.isEmpty() ? null : list.get(0);
	}
}
