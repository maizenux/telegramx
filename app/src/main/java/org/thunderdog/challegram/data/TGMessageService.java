/*
 * This file is a part of Telegram X
 * Copyright © 2014-2022 (tgx-android@pm.me)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * File created on 02/09/2022, 19:31.
 */

package org.thunderdog.challegram.data;

import androidx.annotation.NonNull;

import org.drinkless.td.libcore.telegram.TdApi;
import org.thunderdog.challegram.R;
import org.thunderdog.challegram.component.chat.MessagesManager;
import org.thunderdog.challegram.core.Lang;
import org.thunderdog.challegram.telegram.TdlibSender;
import org.thunderdog.challegram.telegram.TdlibUi;
import org.thunderdog.challegram.theme.Theme;
import org.thunderdog.challegram.ui.MapController;
import org.thunderdog.challegram.util.text.FormattedText;
import org.thunderdog.challegram.util.text.TextColorSet;

import java.util.concurrent.TimeUnit;

import me.vkryl.core.ColorUtils;
import me.vkryl.core.CurrencyUtils;
import me.vkryl.core.StringUtils;
import me.vkryl.td.ChatId;
import me.vkryl.td.Td;

public final class TGMessageService extends TGMessageServiceImpl {
  public TGMessageService (MessagesManager context, TdApi.Message msg, TdApi.MessageContactRegistered contactRegistered) {
    super(context, msg);
    setTextCreator(() ->
      getText(
        R.string.NotificationContactJoined,
        new SenderArgument(sender)
      )
    );
  }

  public TGMessageService (MessagesManager context, TdApi.Message msg, TdApi.MessageScreenshotTaken screenshotTaken) {
    super(context, msg);
    setTextCreator(() -> {
      if (msg.isOutgoing) {
        return getText(R.string.YouTookAScreenshot);
      } else {
        return getText(
          R.string.XTookAScreenshot,
          new SenderArgument(sender, true)
        );
      }
    });
  }

  public TGMessageService (MessagesManager context, TdApi.Message msg, TdApi.MessageExpiredPhoto expiredPhoto) {
    super(context, msg);
    setTextCreator(() ->
      getText(R.string.AttachPhotoExpired)
    );
  }

  public TGMessageService (MessagesManager context, TdApi.Message msg, TdApi.MessageExpiredVideo expiredVideo) {
    super(context, msg);
    setTextCreator(() ->
      getText(R.string.AttachVideoExpired)
    );
  }

  public TGMessageService (MessagesManager context, TdApi.Message msg, TdApi.MessageProximityAlertTriggered proximityAlertTriggered) {
    super(context, msg);
    TdlibSender travelerSender = new TdlibSender(tdlib(), msg.chatId, proximityAlertTriggered.travelerId);
    TdlibSender watcherSender = new TdlibSender(tdlib(), msg.chatId, proximityAlertTriggered.watcherId);
    setTextCreator(() -> {
      boolean inKilometers = proximityAlertTriggered.distance >= 1000;
      int distance = inKilometers ?
        (proximityAlertTriggered.distance / 1000) :
        proximityAlertTriggered.distance;
      if (travelerSender.isSelf()) {
        return getPlural(
          inKilometers ?
            R.string.ProximityAlertYouKM :
            R.string.ProximityAlertYouM,
          distance,
          new SenderArgument(watcherSender)
        );
      } else if (watcherSender.isSelf()) {
        return getPlural(
          inKilometers ?
            R.string.ProximityAlertOtherKM :
            R.string.ProximityAlertOtherM,
          distance,
          new SenderArgument(travelerSender)
        );
      } else {
        return getPlural(
          inKilometers ?
            R.string.ProximityAlertKM :
            R.string.ProximityAlertM,
          distance,
          new SenderArgument(travelerSender),
          new SenderArgument(watcherSender)
        );
      }
    });
  }

  public TGMessageService (MessagesManager context, TdApi.Message msg, TdApi.MessagePinMessage pinMessage) {
    super(context, msg);
    setTextCreator(() -> {
      if (msg.isChannelPost) {
        return getText(R.string.PinnedMessageChanged);
      } else if (msg.isOutgoing) {
        return getText(R.string.YouPinnedMessage);
      } else {
        return getText(
          R.string.NotificationActionPinnedNoTextChannel,
          new SenderArgument(sender)
        );
      }
    });
    setDisplayMessage(msg.chatId, pinMessage.messageId, message -> {
      // TODO: pin
      return false;
    });
  }

  // Group & Channel Messages

  public TGMessageService (MessagesManager context, TdApi.Message msg, TdApi.MessageBasicGroupChatCreate basicGroupCreate) {
    super(context, msg);
    setTextCreator(() -> {
      if (msg.isChannelPost) { // should never be true
        return getText(
          R.string.channel_create_somebody,
          new BoldArgument(basicGroupCreate.title)
        );
      } else if (msg.isOutgoing) {
        return getText(
          R.string.group_create_you,
          new BoldArgument(basicGroupCreate.title)
        );
      } else {
        return getText(
          R.string.group_created,
          new SenderArgument(sender),
          new BoldArgument(basicGroupCreate.title)
        );
      }
    });
  }

  public TGMessageService (MessagesManager context, TdApi.Message msg, TdApi.MessageSupergroupChatCreate supergroupCreate) {
    super(context, msg);
    setTextCreator(() -> {
      if (msg.isChannelPost) {
        return getText(
          R.string.channel_create_somebody,
          new BoldArgument(supergroupCreate.title)
        );
      } else if (msg.isOutgoing) {
        return getText(
          R.string.group_create_you,
          new BoldArgument(supergroupCreate.title)
        );
      } else {
        return getText(
          R.string.group_created,
          new SenderArgument(sender),
          new BoldArgument(supergroupCreate.title)
        );
      }
    });
  }

  public TGMessageService (MessagesManager context, TdApi.Message msg, TdApi.MessageChatChangeTitle changeTitle) {
    super(context, msg);
    setTextCreator(() -> {
      if (msg.isChannelPost) {
        return getText(
          R.string.ChannelRenamed,
          new BoldArgument(changeTitle.title)
        );
      } else if (msg.isOutgoing) {
        return getText(
          R.string.group_title_changed_you,
          new BoldArgument(changeTitle.title)
        );
      } else {
        return getText(
          R.string.group_title_changed,
          new SenderArgument(sender),
          new BoldArgument(changeTitle.title)
        );
      }
    });
  }

  public TGMessageService (MessagesManager context, TdApi.Message msg, TdApi.MessageChatChangePhoto changePhoto) {
    super(context, msg);
    setTextCreator(() -> {
      if (msg.isChannelPost) {
        return getText(R.string.ActionChannelChangedPhoto);
      } else if (msg.isOutgoing) {
        return getText(R.string.group_photo_changed_you);
      } else {
        return getText(
          R.string.group_photo_changed,
          new SenderArgument(sender)
        );
      }
    });
    setDisplayChatPhoto(changePhoto.photo);
  }

  public TGMessageService (MessagesManager context, TdApi.Message msg, TdApi.MessageChatDeletePhoto deletePhoto) {
    super(context, msg);
    setTextCreator(() -> {
      if (msg.isChannelPost) {
        return getText(R.string.ActionChannelRemovedPhoto);
      } else if (msg.isOutgoing) {
        return getText(R.string.group_photo_deleted_you);
      } else {
        return getText(
          R.string.group_photo_deleted,
          new SenderArgument(sender)
        );
      }
    });
  }

  public TGMessageService (MessagesManager context, TdApi.Message msg, TdApi.MessageChatUpgradeTo upgradeToSupergroup) {
    super(context, msg);
    setTextCreator(() ->
      getText(
        upgradeToSupergroup.supergroupId != 0 ?
          R.string.GroupUpgradedTo :
          R.string.GroupUpgraded
      )
    );
    if (upgradeToSupergroup.supergroupId != 0) {
      setOnClickListener(() ->
        tdlib.ui().openSupergroupChat(controller(), upgradeToSupergroup.supergroupId, new TdlibUi.ChatOpenParameters().urlOpenParameters(openParameters()))
      );
    }
  }

  public TGMessageService (MessagesManager context, TdApi.Message msg, TdApi.MessageChatUpgradeFrom upgradeFromBasicGroup) {
    super(context, msg);
    setTextCreator(() ->
      getText(
        upgradeFromBasicGroup.basicGroupId != 0 ?
          R.string.GroupUpgradedFrom :
          R.string.GroupUpgraded
      )
    );
    if (upgradeFromBasicGroup.basicGroupId != 0) {
      setOnClickListener(() ->
        tdlib.ui().openBasicGroupChat(controller(), upgradeFromBasicGroup.basicGroupId, new TdlibUi.ChatOpenParameters().urlOpenParameters(openParameters()))
      );
    }
  }

  public TGMessageService (MessagesManager context, TdApi.Message msg, TdApi.MessageChatJoinByLink joinByLink) {
    super(context, msg);
    setTextCreator(() -> {
      if (msg.isOutgoing) {
        return getText(
          msg.isChannelPost ?
            R.string.YouJoinedByLink :
            R.string.group_user_join_by_link_self
        );
      } else {
        return getText(
          msg.isChannelPost ?
            R.string.XJoinedByLink :
            R.string.group_user_join_by_link,
          new SenderArgument(sender)
        );
      }
    });
  }

  public TGMessageService (MessagesManager context, TdApi.Message msg, TdApi.MessageChatJoinByRequest joinByRequest) {
    super(context, msg);
    setTextCreator(() -> {
      if (msg.isOutgoing) {
        return getText(
          msg.isChannelPost ?
            R.string.YouAcceptedToChannel :
            R.string.YouAcceptedToGroup
        );
      } else {
        return getText(
          msg.isChannelPost ?
            R.string.XAcceptedToChannel :
            R.string.XAcceptedToGroup,
          new SenderArgument(sender)
        );
      }
    });
  }

  public TGMessageService (MessagesManager context, TdApi.Message msg, TdApi.MessageChatAddMembers addMembers) {
    super(context, msg);
    TdlibSender[] addedMembers = TdlibSender.valueOfUserIds(tdlib(), msg.chatId, addMembers.memberUserIds);
    setTextCreator(() -> {
      TdlibSender targetMember =
        addedMembers.length == 1 ?
          addedMembers[0] :
          null;
      if (sender.isSelf()) {
        if (sender.isSameSender(targetMember)) {
          return getText(
            msg.isChannelPost ?
              R.string.channel_user_add_self :
              R.string.group_user_add_self
          );
        } else {
          return getText(
            R.string.group_user_self_added,
            addedMembers.length == 1 ?
              new SenderArgument(addedMembers[0]) :
              new SenderListArgument(addedMembers)
          );
        }
      } else {
        if (sender.isSameSender(targetMember)) {
          return getText(
            msg.isChannelPost ?
              R.string.channel_user_add :
              R.string.group_user_add,
            new SenderArgument(sender)
          );
        } else if (targetMember != null && targetMember.isSelf()) {
          return getText(
            R.string.group_user_added_self,
            new SenderArgument(sender)
          );
        } else {
          return getText(
            R.string.group_user_added,
            new SenderArgument(sender),
            addedMembers.length == 1 ?
              new SenderArgument(addedMembers[0]) :
              new SenderListArgument(addedMembers)
          );
        }
      }
    });
  }

  public TGMessageService (MessagesManager context, TdApi.Message msg, TdApi.MessageChatDeleteMember deleteMember) {
    super(context, msg);
    TdlibSender targetSender = new TdlibSender(tdlib, msg.chatId, new TdApi.MessageSenderUser(deleteMember.userId));
    setTextCreator(() -> {
      if (sender.isSameSender(targetSender)) {
        if (sender.isSelf()) {
          return getText(
            msg.isChannelPost ?
              R.string.channel_user_remove_self :
              R.string.group_user_remove_self
          );
        } else {
          return getText(
            msg.isChannelPost ?
              R.string.channel_user_remove :
              R.string.group_user_remove,
            new SenderArgument(sender)
          );
        }
      } else {
        if (sender.isSelf()) {
          return getText(
            R.string.group_user_self_removed,
            new SenderArgument(targetSender)
          );
        } else if (targetSender.isSelf()) {
          return getText(
            R.string.group_user_removed_self,
            new SenderArgument(sender)
          );
        } else {
          return getText(
            R.string.group_user_removed,
            new SenderArgument(sender),
            new SenderArgument(targetSender)
          );
        }
      }
    });
  }

  public TGMessageService (MessagesManager context, TdApi.Message msg, TdApi.MessageWebsiteConnected websiteConnected) {
    super(context, msg);
    setTextCreator(() ->
      getText(
        R.string.BotWebsiteAllowed,
        new BoldArgument(websiteConnected.domainName)
      )
    );
  }

  public TGMessageService (MessagesManager context, TdApi.Message msg, TdApi.MessageChatSetTtl setTtl) {
    super(context, msg);
    setTextCreator(() -> {
      boolean isUserChat = ChatId.isUserChat(msg.chatId);
      if (setTtl.ttl == 0) {
        if (msg.isOutgoing) {
          return getText(
            isUserChat ?
              R.string.YouDisabledTimer :
              R.string.YouDisabledAutoDelete
          );
        } else {
          return getText(
            isUserChat ?
              R.string.XDisabledTimer :
            msg.isChannelPost ?
              R.string.XDisabledAutoDeletePosts :
              R.string.XDisabledAutoDelete,
            new SenderArgument(sender, true)
          );
        }
      } else if (isUserChat) {
        if (msg.isOutgoing) {
          return getDuration(
            R.string.YouSetTimerSeconds, R.string.YouSetTimerMinutes, R.string.YouSetTimerHours, R.string.YouSetTimerDays, R.string.YouSetTimerWeeks, R.string.YouSetTimerMonths,
            setTtl.ttl, TimeUnit.SECONDS
          );
        } else {
          return getDuration(
            R.string.XSetTimerSeconds, R.string.XSetTimerMinutes, R.string.XSetTimerHours, R.string.XSetTimerDays, R.string.XSetTimerWeeks, R.string.XSetTimerMonths,
            setTtl.ttl, TimeUnit.SECONDS,
            new SenderArgument(sender, true)
          );
        }
      } else if (msg.isChannelPost) {
        if (msg.isOutgoing) {
          return getDuration(
            R.string.YouSetAutoDeletePostsSeconds, R.string.YouSetAutoDeletePostsMinutes, R.string.YouSetAutoDeletePostsHours, R.string.YouSetAutoDeletePostsDays, R.string.YouSetAutoDeletePostsWeeks, R.string.YouSetAutoDeletePostsMonths,
            setTtl.ttl, TimeUnit.SECONDS
          );
        } else {
          return getDuration(
            R.string.XSetAutoDeletePostsSeconds, R.string.XSetAutoDeletePostsMinutes, R.string.XSetAutoDeletePostsHours, R.string.XSetAutoDeletePostsDays, R.string.XSetAutoDeletePostsWeeks, R.string.XSetAutoDeletePostsMonths,
            setTtl.ttl, TimeUnit.SECONDS,
            new SenderArgument(sender, true)
          );
        }
      } else {
        if (msg.isOutgoing) {
          return getDuration(
            R.string.YouSetAutoDeleteSeconds, R.string.YouSetAutoDeleteMinutes, R.string.YouSetAutoDeleteHours, R.string.YouSetAutoDeleteDays, R.string.YouSetAutoDeleteWeeks, R.string.YouSetAutoDeleteMonths,
            setTtl.ttl, TimeUnit.SECONDS
          );
        } else {
          return getDuration(
            R.string.XSetAutoDeleteSeconds, R.string.XSetAutoDeleteMinutes, R.string.XSetAutoDeleteHours, R.string.XSetAutoDeleteDays, R.string.XSetAutoDeleteWeeks, R.string.XSetAutoDeleteMonths,
            setTtl.ttl, TimeUnit.SECONDS,
            new SenderArgument(sender, true)
          );
        }
      }
    });
  }

  public TGMessageService (MessagesManager context, TdApi.Message msg, TdApi.MessageGameScore gameScore) {
    super(context, msg);
    setTextCreator(() -> {
      if (msg.isOutgoing) {
        return getPlural(
          R.string.game_ActionYouScored,
          gameScore.score
        );
      } else {
        return getPlural(
          R.string.game_ActionUserScored,
          gameScore.score,
          new SenderArgument(sender)
        );
      }
    });
    if (gameScore.gameMessageId != 0) {
      setDisplayMessage(msg.chatId, gameScore.gameMessageId, (message) -> {
        if (message.content.getConstructor() != TdApi.MessageGame.CONSTRUCTOR) {
          return false;
        }
        setTextCreator(() -> {
          if (msg.isOutgoing) {
            return getPlural(
              R.string.game_ActionYouScoredInGame,
              gameScore.score,
              new GameArgument(message)
            );
          } else {
            return getPlural(
              R.string.game_ActionUserScoredInGame,
              gameScore.score,
              new SenderArgument(sender),
              new GameArgument(message)
            );
          }
        });
        return true;
      });
    }
  }

  public TGMessageService (MessagesManager context, TdApi.Message msg, TdApi.MessagePaymentSuccessful paymentSuccessful) {
    super(context, msg); // TODO: recurring payment strings
    String amount = CurrencyUtils.buildAmount(paymentSuccessful.currency, paymentSuccessful.totalAmount);
    setTextCreator(() ->
      getText(
        R.string.PaymentSuccessfullyPaidNoItem,
        new BoldArgument(amount),
        new SenderArgument(sender)
      )
    );
    if (paymentSuccessful.invoiceChatId != 0 && paymentSuccessful.invoiceMessageId != 0) {
      setDisplayMessage(
        paymentSuccessful.invoiceChatId,
        paymentSuccessful.invoiceMessageId,
        message -> {
          if (message.content.getConstructor() != TdApi.MessageInvoice.CONSTRUCTOR) {
            return false;
          }
          setTextCreator(() ->
            getText(
              R.string.PaymentSuccessfullyPaid,
              new BoldArgument(amount),
              new SenderArgument(sender),
              new InvoiceArgument(message)
            )
          );
          return true;
        }
      );
    }
  }

  // Video chats

  public TGMessageService (MessagesManager context, TdApi.Message msg, TdApi.MessageVideoChatStarted videoChatStarted) {
    super(context, msg);
    setTextCreator(() -> {
      if (msg.isOutgoing) {
        return getText(
          msg.isChannelPost ?
            R.string.LiveStreamStartedYou :
            R.string.VoiceChatStartedYou
        );
      } else if (sender.isAnonymousGroupAdmin()) {
        return getText(
          msg.isChannelPost ?
            R.string.LiveStreamStarted :
            R.string.VoiceChatStarted
        );
      } else {
        return getText(
          msg.isChannelPost ?
            R.string.LiveStreamStartedBy :
            R.string.VoiceChatStartedBy,
          new SenderArgument(sender)
        );
      }
    });
    setOnClickListener(() ->
      tdlib.ui().openVoiceChat(controller(), videoChatStarted.groupCallId, openParameters())
    );
  }

  public TGMessageService (MessagesManager context, TdApi.Message msg, TdApi.MessageVideoChatScheduled videoChatScheduled) {
    super(context, msg);
    setTextCreator(() -> {
      String timestamp = Lang.getMessageTimestamp(videoChatScheduled.startDate, TimeUnit.SECONDS);
      return getText(
        msg.isChannelPost ?
          R.string.LiveStreamScheduledOn :
          R.string.VideoChatScheduledFor,
        new BoldArgument(timestamp)
      );
    });
    setOnClickListener(() ->
      tdlib.ui().openVoiceChat(controller(), videoChatScheduled.groupCallId, openParameters())
    );
  }

  public TGMessageService (MessagesManager context, TdApi.Message msg, TdApi.MessageVideoChatEnded videoChatEnded) {
    super(context, msg);
    setTextCreator(() -> {
      String duration = Lang.getCallDuration(videoChatEnded.duration);
      if (msg.isOutgoing) {
        return getText(
          msg.isChannelPost ?
            R.string.LiveStreamEndedYou :
            R.string.VoiceChatEndedYou,
          new BoldArgument(duration)
        );
      } else if (sender.isAnonymousGroupAdmin()) {
        return getText(
          msg.isChannelPost ?
            R.string.LiveStreamEnded :
            R.string.VoiceChatEnded,
          new BoldArgument(duration)
        );
      } else {
        return getText(
          msg.isChannelPost ?
            R.string.LiveStreamEndedBy :
            R.string.VoiceChatEndedBy,
          new SenderArgument(sender),
          new BoldArgument(duration)
        );
      }
    });
  }

  public TGMessageService (MessagesManager context, TdApi.Message msg, TdApi.MessageInviteVideoChatParticipants inviteVideoChatParticipants) {
    super(context, msg);
    TdlibSender[] invitedParticipants = TdlibSender.valueOfUserIds(tdlib(), msg.chatId, inviteVideoChatParticipants.userIds);
    setTextCreator(() -> {
      if (sender.isSelf() || msg.isOutgoing) {
        return getText(
          msg.isChannelPost ?
            R.string.LiveStreamInviteOther :
            R.string.VoiceChatInviteOther,
          invitedParticipants.length == 1 ?
            new SenderArgument(invitedParticipants[0]) :
            new SenderListArgument(invitedParticipants)
        );
      } else if (invitedParticipants.length == 1 && invitedParticipants[0].isSelf()) {
        return getText(
          msg.isChannelPost ?
            R.string.LiveStreamInviteSelf :
            R.string.VoiceChatInviteSelf,
          new SenderArgument(sender)
        );
      } else {
        return getText(
          msg.isChannelPost ?
            R.string.LiveStreamInvite :
            R.string.VoiceChatInvite,
          new SenderArgument(sender),
          new SenderListArgument(invitedParticipants)
        );
      }
    });
  }

  // Custom server's service message

  public TGMessageService (MessagesManager context, TdApi.Message msg, TdApi.MessageCustomServiceAction custom) {
    super(context, msg);
    setTextCreator(() ->
      new FormattedText(custom.text)
    );
  }

  // Recent actions (chat events)

  public TGMessageService (MessagesManager context, TdApi.Message msg, TdApi.ChatEventMessageDeleted messageDeleted) {
    super(context, msg);
    setTextCreator(() ->
      getText(
        R.string.EventLogDeletedMessages,
        new SenderArgument(sender)
      )
    );
  }

  public TGMessageService (MessagesManager context, TdApi.Message msg, TdApi.ChatEventMessageEdited messageEdited) {
    super(context, msg);
    setTextCreator(() -> {
      if (messageEdited.newMessage.content.getConstructor() == TdApi.MessageText.CONSTRUCTOR ||
          messageEdited.newMessage.content.getConstructor() == TdApi.MessageAnimatedEmoji.CONSTRUCTOR) {
        return getText(R.string.EventLogEditedMessages, new SenderArgument(sender));
      } else if (Td.isEmpty(Td.textOrCaption(messageEdited.newMessage.content))) {
        return getText(R.string.EventLogRemovedCaption, new SenderArgument(sender));
      } else {
        return getText(R.string.EventLogEditedCaption, new SenderArgument(sender));
      }
    });
  }

  public TGMessageService (MessagesManager context, TdApi.Message msg, TdApi.ChatEventPollStopped pollStopped) {
    super(context, msg);
    setTextCreator(() -> {
      final boolean isQuiz =
        pollStopped.message.content.getConstructor() == TdApi.MessagePoll.CONSTRUCTOR &&
        ((TdApi.MessagePoll) pollStopped.message.content).poll.type.getConstructor() == TdApi.PollTypeQuiz.CONSTRUCTOR;
      return getText(
        isQuiz ?
          R.string.EventLogQuizStopped :
          R.string.EventLogPollStopped,
        new SenderArgument(sender)
      );
    });
  }

  public TGMessageService (MessagesManager context, TdApi.Message msg, TdApi.ChatEventMessageUnpinned messageUnpinned) {
    super(context, msg);
    setTextCreator(() ->
      getText(
        R.string.EventLogUnpinnedMessages,
        new SenderArgument(sender)
      )
    );
  }

  public TGMessageService (MessagesManager context, TdApi.Message msg, TdApi.ChatEventInvitesToggled invitesToggled) {
    super(context, msg);
    setTextCreator(() ->
      getText(
        invitesToggled.canInviteUsers ?
          R.string.EventLogToggledInvitesOn :
          R.string.EventLogToggledInvitesOff,
        new SenderArgument(sender)
      )
    );
  }

  public TGMessageService (MessagesManager context, TdApi.Message msg, TdApi.ChatEventSignMessagesToggled signMessagesToggled) {
    super(context, msg);
    setTextCreator(() ->
      getText(
        signMessagesToggled.signMessages ?
          R.string.EventLogToggledSignaturesOn :
          R.string.EventLogToggledSignaturesOff,
        new SenderArgument(sender)
      )
    );
  }

  public TGMessageService (MessagesManager context, TdApi.Message msg, TdApi.ChatEventHasProtectedContentToggled protectedContentToggled) {
    super(context, msg);
    setTextCreator(() ->
      getText(
        protectedContentToggled.hasProtectedContent ?
          R.string.EventLogToggledProtectionOn :
          R.string.EventLogToggledProtectionOff,
        new SenderArgument(sender)
      )
    );
  }

  public TGMessageService (MessagesManager context, TdApi.Message msg, TdApi.ChatEventIsAllHistoryAvailableToggled isAllHistoryAvailableToggled) {
    super(context, msg);
    setTextCreator(() ->
      getText(
        isAllHistoryAvailableToggled.isAllHistoryAvailable ?
          R.string.XMadeGroupHistoryVisible :
          R.string.XMadeGroupHistoryHidden,
        new SenderArgument(sender)
      )
    );
  }

  public TGMessageService (MessagesManager context, TdApi.Message msg, TdApi.ChatEventVideoChatMuteNewParticipantsToggled newParticipantsToggled) {
    super(context, msg);
    setTextCreator(() -> {
      if (msg.isChannelPost) {
        if (msg.isOutgoing) {
          return getText(
            newParticipantsToggled.muteNewParticipants ?
              R.string.EventLogChannelMutedNewParticipantsYou :
              R.string.EventLogChannelUnmutedNewParticipantsYou
          );
        } else {
          return getText(
            newParticipantsToggled.muteNewParticipants ?
              R.string.EventLogChannelMutedNewParticipants :
              R.string.EventLogChannelUnmutedNewParticipants,
            new SenderArgument(sender)
          );
        }
      } else {
        if (msg.isOutgoing) {
          return getText(
            newParticipantsToggled.muteNewParticipants ?
              R.string.EventLogMutedNewParticipantsYou :
              R.string.EventLogUnmutedNewParticipantsYou
          );
        } else {
          return getText(
            newParticipantsToggled.muteNewParticipants ?
              R.string.EventLogMutedNewParticipants :
              R.string.EventLogUnmutedNewParticipants,
            new SenderArgument(sender)
          );
        }
      }
    });
  }

  public TGMessageService (MessagesManager context, TdApi.Message msg, TdApi.ChatEventVideoChatParticipantIsMutedToggled videoChatParticipantIsMutedToggled) {
    super(context, msg);
    TdlibSender targetSender = new TdlibSender(tdlib, msg.chatId, videoChatParticipantIsMutedToggled.participantId);
    setTextCreator(() -> {
      if (msg.isChannelPost) {
        if (msg.isOutgoing) {
          return getText(
            videoChatParticipantIsMutedToggled.isMuted ?
              R.string.EventLogChannelMutedParticipantYou :
              R.string.EventLogChannelUnmutedParticipantYou,
            new SenderArgument(targetSender)
          );
        } else {
          return getText(
            videoChatParticipantIsMutedToggled.isMuted ?
              R.string.EventLogChannelMutedParticipant :
              R.string.EventLogChannelUnmutedParticipant,
            new SenderArgument(sender),
            new SenderArgument(targetSender)
          );
        }
      } else {
        if (msg.isOutgoing) {
          return getText(
            videoChatParticipantIsMutedToggled.isMuted ?
              R.string.EventLogMutedParticipantYou :
              R.string.EventLogUnmutedParticipantYou,
            new SenderArgument(targetSender)
          );
        } else {
          return getText(
            videoChatParticipantIsMutedToggled.isMuted ?
              R.string.EventLogMutedParticipant :
              R.string.EventLogUnmutedParticipant,
            new SenderArgument(sender),
            new SenderArgument(targetSender)
          );
        }
      }
    });
  }

  public TGMessageService (MessagesManager context, TdApi.Message msg, TdApi.ChatEventVideoChatParticipantVolumeLevelChanged videoChatParticipantVolumeLevelChanged) {
    super(context, msg);
    TdlibSender targetSender = new TdlibSender(tdlib, msg.chatId, videoChatParticipantVolumeLevelChanged.participantId);
    setTextCreator(() -> {
      final FormattedArgument volume = new BoldArgument((videoChatParticipantVolumeLevelChanged.volumeLevel / 100) + "%");
      if (msg.isOutgoing) {
        return getText(
          R.string.EventLogChangedVolumeYou,
          new SenderArgument(targetSender),
          volume
        );
      } else if (targetSender.isSelf()) {
        return getText(
          R.string.EventLogChangedYourVolume,
          new SenderArgument(sender),
          volume
        );
      } else {
        return getText(
          R.string.EventLogChangedVolume,
          new SenderArgument(sender),
          new SenderArgument(targetSender),
          volume
        );
      }
    });
  }

  public TGMessageService (MessagesManager context, TdApi.Message msg, TdApi.ChatEventUsernameChanged usernameChanged) {
    super(context, msg);
    setTextCreator(() -> {
      boolean hasUsername = !StringUtils.isEmpty(usernameChanged.newUsername);
      if (msg.isChannelPost) {
        return getText(
          hasUsername ?
            R.string.EventLogChangedChannelLink :
            R.string.EventLogRemovedChannelLink,
          new SenderArgument(sender)
        );
      } else {
        return getText(
          hasUsername ?
            R.string.EventLogChangedGroupLink :
            R.string.EventLogRemovedGroupLink,
          new SenderArgument(sender)
        );
      }
    });
  }

  public TGMessageService (MessagesManager context, TdApi.Message msg, TdApi.ChatEventDescriptionChanged descriptionChanged) {
    super(context, msg);
    setTextCreator(() -> {
      boolean hasDescription = !StringUtils.isEmpty(descriptionChanged.newDescription);
      if (msg.isChannelPost) {
        return getText(
          hasDescription ?
            R.string.EventLogEditedChannelDescription :
            R.string.EventLogRemovedChannelDescription,
          new SenderArgument(sender)
        );
      } else {
        return getText(
          hasDescription ?
            R.string.EventLogEditedGroupDescription :
            R.string.EventLogRemovedGroupDescription,
          new SenderArgument(sender)
        );
      }
    });
  }

  public TGMessageService (MessagesManager context, TdApi.Message msg, TdApi.ChatEventStickerSetChanged stickerSetChanged) {
    super(context, msg);
    setTextCreator(() ->
      getText(
        stickerSetChanged.newStickerSetId != 0 ?
          R.string.XChangedGroupStickerSet :
          R.string.XRemovedGroupStickerSet,
        new SenderArgument(sender)
      )
    );
    long stickerSetId = stickerSetChanged.newStickerSetId != 0 ?
      stickerSetChanged.newStickerSetId :
      stickerSetChanged.oldStickerSetId;
    if (stickerSetId != 0) {
      setOnClickListener(() ->
        tdlib.ui().showStickerSet(controller(), stickerSetId, openParameters())
      );
    }
  }

  public TGMessageService (MessagesManager context, TdApi.Message msg, TdApi.ChatEventLinkedChatChanged linkedChatChanged) {
    super(context, msg);
    TdlibSender linkedChat = new TdlibSender(tdlib, msg.chatId, new TdApi.MessageSenderChat(
      linkedChatChanged.newLinkedChatId != 0 ?
        linkedChatChanged.newLinkedChatId :
        linkedChatChanged.oldLinkedChatId
    ));
    setTextCreator(() -> {
      boolean hasLinkedChat = linkedChatChanged.newLinkedChatId != 0;
      if (msg.isChannelPost) {
        return getText(
          hasLinkedChat ?
            R.string.EventLogLinkedGroupChanged :
            R.string.EventLogLinkedGroupRemoved,
          new SenderArgument(sender),
          new SenderArgument(linkedChat)
        );
      } else if (sender.isServiceChannelBot() || Td.getSenderId(msg) == msg.chatId) {
        return getText(
          hasLinkedChat ?
            R.string.EventLogLinkedChannelChangedUnknown :
            R.string.EventLogLinkedChannelRemovedUnknown,
          new SenderArgument(linkedChat)
        );
      } else {
        return getText(
          hasLinkedChat ?
            R.string.EventLogLinkedChannelChanged :
            R.string.EventLogLinkedChannelRemoved,
          new SenderArgument(sender),
          new SenderArgument(linkedChat)
        );
      }
    });
  }

  public TGMessageService (MessagesManager context, TdApi.Message msg, TdApi.ChatEventLocationChanged locationChanged) {
    super(context, msg);
    setTextCreator(() -> {
      if (locationChanged.newLocation != null) {
        return getText(
          locationChanged.oldLocation != null ?
            R.string.EventLogLocationChanged :
            R.string.EventLogLocationSet,
          new SenderArgument(sender),
          new BoldArgument(locationChanged.newLocation.address)
        );
      } else {
        return getText( // TODO: display locationChanged.oldLocation.address
          R.string.EventLogLocationRemoved,
          new SenderArgument(sender)
        );
      }
    });
    TdApi.ChatLocation chatLocation = locationChanged.newLocation != null ?
      locationChanged.newLocation :
      locationChanged.oldLocation;
    if (chatLocation != null) {
      setOnClickListener(() ->
        tdlib.ui().openMap(this, new MapController.Args(
            chatLocation.location.latitude,
            chatLocation.location.longitude
          ).setChatId(msg.chatId, messagesController().getMessageThreadId())
            .setLocationOwnerChatId(msg.chatId)
            .setIsFaded(locationChanged.newLocation == null)
        )
      );
    }
  }

  public TGMessageService (MessagesManager context, TdApi.Message msg, TdApi.ChatEventPhotoChanged photoChanged) {
    super(context, msg);
    setTextCreator(() -> {
      if (photoChanged.newPhoto != null) {
        if (msg.isChannelPost) {
          return getText(R.string.ActionChannelChangedPhoto);
        } else if (msg.isOutgoing) {
          return getText(R.string.group_photo_changed_you);
        } else {
          return getText(R.string.group_photo_changed, new SenderArgument(sender));
        }
      } else {
        if (msg.isChannelPost) {
          return getText(R.string.ActionChannelRemovedPhoto);
        } else if (msg.isOutgoing) {
          return getText(R.string.group_photo_deleted_you);
        } else {
          return getText(R.string.group_photo_deleted, new SenderArgument(sender));
        }
      }
    });
    TdApi.ChatPhoto chatPhoto = photoChanged.newPhoto != null ?
      photoChanged.newPhoto :
      photoChanged.oldPhoto;
    if (chatPhoto != null) {
      setDisplayChatPhoto(chatPhoto);
    }
  }

  public TGMessageService (MessagesManager context, TdApi.Message msg, TdApi.ChatEventSlowModeDelayChanged slowModeDelayChanged) {
    super(context, msg);
    setTextCreator(() -> {
      if (slowModeDelayChanged.newSlowModeDelay != 0) {
        String duration = Lang.getDuration(slowModeDelayChanged.newSlowModeDelay);
        if (msg.isOutgoing) {
          return getText(
            R.string.EventLogSlowModeChangedYou,
            new BoldArgument(duration)
          );
        } else {
          return getText(
            R.string.EventLogSlowModeChanged,
            new SenderArgument(sender),
            new BoldArgument(duration)
          );
        }
      } else {
        return getText(
          R.string.EventLogSlowModeDisabled,
          new SenderArgument(sender)
        );
      }
    });
  }

  public TGMessageService (MessagesManager context, TdApi.Message msg, TdApi.ChatEventMessagePinned messagePinned) {
    super(context, msg);
    setTextCreator(() ->
      getText(R.string.EventLogPinnedMessages, new SenderArgument(sender))
    );
  }

  public TGMessageService (MessagesManager context, TdApi.Message msg, TdApi.ChatEventMemberJoinedByInviteLink joinedByInviteLink) {
    super(context, msg);
    TdlibSender linkAuthor = new TdlibSender(tdlib(), msg.chatId, new TdApi.MessageSenderUser(joinedByInviteLink.inviteLink.creatorUserId));
    setTextCreator(() -> {
      if (joinedByInviteLink.inviteLink.isPrimary) {
        if (msg.isOutgoing) {
          return getText(
            msg.isChannelPost ?
              R.string.LinkJoinChannelPrimaryYou :
              R.string.LinkJoinPrimaryYou,
            new InviteLinkArgument(joinedByInviteLink.inviteLink)
          );
        } else {
          return getText(
            msg.isChannelPost ?
              R.string.LinkJoinChannelPrimary :
              R.string.LinkJoinPrimary,
            new SenderArgument(sender),
            new InviteLinkArgument(joinedByInviteLink.inviteLink)
          );
        }
      } else {
        if (msg.isOutgoing) {
          return getText(
            Td.isTemporary(joinedByInviteLink.inviteLink) ?
              msg.isChannelPost ?
                R.string.LinkJoinChannelTempYou :
                R.string.LinkJoinTempYou :
              msg.isChannelPost ?
                R.string.LinkJoinChannelOtherYou :
                R.string.LinkJoinOtherYou,
            new SenderArgument(linkAuthor),
            new InviteLinkArgument(joinedByInviteLink.inviteLink)
          );
        } else {
          return getText(
            Td.isTemporary(joinedByInviteLink.inviteLink) ?
              msg.isChannelPost ?
                R.string.LinkJoinChannelTemp :
                R.string.LinkJoinTemp :
              msg.isChannelPost ?
                R.string.LinkJoinChannelOther :
                R.string.LinkJoinOther,
            new SenderArgument(sender),
            new SenderArgument(linkAuthor),
            new InviteLinkArgument(joinedByInviteLink.inviteLink)
          );
        }
      }
    });
  }

  public TGMessageService (MessagesManager context, TdApi.Message msg, TdApi.ChatEventMemberJoinedByRequest joinedByRequest) {
    super(context, msg);
    TdlibSender approvedBy = new TdlibSender(tdlib(), msg.chatId, new TdApi.MessageSenderUser(joinedByRequest.approverUserId));
    TdlibSender linkAuthor =
      joinedByRequest.inviteLink != null ?
        new TdlibSender(tdlib(), msg.chatId, new TdApi.MessageSenderUser(joinedByRequest.inviteLink.creatorUserId)) :
        null;
    setTextCreator(() -> {
      if (joinedByRequest.inviteLink != null) {
        if (joinedByRequest.inviteLink.isPrimary) {
          if (msg.isOutgoing) {
            return getText(
              msg.isChannelPost ?
                R.string.LinkJoinChannelPrimaryYouWithApproval :
                R.string.LinkJoinPrimaryYouWithApproval,
              new InviteLinkArgument(joinedByRequest.inviteLink),
              new SenderArgument(approvedBy)
            );
          } else {
            return getText(
              msg.isChannelPost ?
                R.string.LinkJoinChannelPrimaryWithApproval :
                R.string.LinkJoinPrimaryWithApproval,
              new SenderArgument(sender),
              new InviteLinkArgument(joinedByRequest.inviteLink),
              new SenderArgument(approvedBy)
            );
          }
        } else {
          if (msg.isOutgoing) {
            return getText(
              Td.isTemporary(joinedByRequest.inviteLink) ?
                msg.isChannelPost ?
                  R.string.LinkJoinChannelTempYouWithApproval :
                  R.string.LinkJoinTempYouWithApproval :
                msg.isChannelPost ?
                  R.string.LinkJoinChannelOtherYouWithApproval :
                  R.string.LinkJoinOtherYouWithApproval,
              new SenderArgument(linkAuthor),
              new InviteLinkArgument(joinedByRequest.inviteLink),
              new SenderArgument(approvedBy)
            );
          } else {
            return getText(
              Td.isTemporary(joinedByRequest.inviteLink) ?
                msg.isChannelPost ?
                  R.string.LinkJoinChannelTempWithApproval :
                  R.string.LinkJoinTempWithApproval :
                msg.isChannelPost ?
                  R.string.LinkJoinChannelOtherWithApproval :
                  R.string.LinkJoinOtherWithApproval,
              new SenderArgument(sender),
              new SenderArgument(linkAuthor),
              new InviteLinkArgument(joinedByRequest.inviteLink),
              new SenderArgument(approvedBy)
            );
          }
        }
      } else {
        if (msg.isOutgoing) {
          return getText(
            msg.isChannelPost ?
              R.string.YouAcceptedToChannelBy :
              R.string.YouAcceptedToGroupBy,
            new SenderArgument(approvedBy)
          );
        } else {
          return getText(
            msg.isChannelPost ?
              R.string.XAcceptedToChannelBy :
              R.string.XAcceptedToGroupBy,
            new SenderArgument(sender),
            new SenderArgument(approvedBy)
          );
        }
      }
    });
  }

  public TGMessageService (MessagesManager context, TdApi.Message msg, TdApi.ChatEventInviteLinkRevoked inviteLinkRevoked) {
    super(context, msg);
    TdlibSender linkAuthor = new TdlibSender(tdlib(), msg.chatId, new TdApi.MessageSenderUser(inviteLinkRevoked.inviteLink.creatorUserId));
    setTextCreator(() -> {
      if (inviteLinkRevoked.inviteLink.isPrimary) {
        if (msg.isOutgoing) {
          return getText(
            R.string.LinkRevokePrimaryYou,
            new InviteLinkArgument(inviteLinkRevoked.inviteLink)
          );
        } else {
          return getText(
            R.string.LinkRevokePrimary,
            new SenderArgument(sender),
            new InviteLinkArgument(inviteLinkRevoked.inviteLink)
          );
        }
      } else {
        if (msg.isOutgoing) {
          return getText(
            Td.isTemporary(inviteLinkRevoked.inviteLink) ?
              R.string.LinkRevokeTempYou :
              R.string.LinkRevokeOtherYou,
            new SenderArgument(linkAuthor),
            new InviteLinkArgument(inviteLinkRevoked.inviteLink)
          );
        } else {
          return getText(
            Td.isTemporary(inviteLinkRevoked.inviteLink) ?
              R.string.LinkRevokeTemp :
              R.string.LinkRevokeOther,
            new SenderArgument(sender),
            new SenderArgument(linkAuthor),
            new InviteLinkArgument(inviteLinkRevoked.inviteLink)
          );
        }
      }
    });
  }

  public TGMessageService (MessagesManager context, TdApi.Message msg, TdApi.ChatEventInviteLinkDeleted inviteLinkDeleted) {
    super(context, msg);
    TdlibSender linkAuthor = new TdlibSender(tdlib(), msg.chatId, new TdApi.MessageSenderUser(inviteLinkDeleted.inviteLink.creatorUserId));
    setTextCreator(() -> {
      if (inviteLinkDeleted.inviteLink.isPrimary) {
        if (msg.isOutgoing) {
          return getText(
            R.string.LinkDeletePrimaryYou,
            new InviteLinkArgument(inviteLinkDeleted.inviteLink)
          );
        } else {
          return getText(
            R.string.LinkDeletePrimary,
            new SenderArgument(sender),
            new InviteLinkArgument(inviteLinkDeleted.inviteLink)
          );
        }
      } else {
        if (msg.isOutgoing) {
          return getText(
            Td.isTemporary(inviteLinkDeleted.inviteLink) ?
              R.string.LinkDeleteTempYou :
              R.string.LinkDeleteOtherYou,
            new SenderArgument(linkAuthor),
            new InviteLinkArgument(inviteLinkDeleted.inviteLink)
          );
        } else {
          return getText(
            Td.isTemporary(inviteLinkDeleted.inviteLink) ?
              R.string.LinkDeleteTemp :
              R.string.LinkDeleteOther,
            new SenderArgument(sender),
            new SenderArgument(linkAuthor),
            new InviteLinkArgument(inviteLinkDeleted.inviteLink)
          );
        }
      }
    });
  }

  // Pre-impl: utilities used by constructors

  @Override
  @NonNull
  protected TextColorSet defaultTextColorSet () {
    if (useBubbles()) {
      return new TextColorSet() {
        @Override
        public int defaultTextColor () {
          return getBubbleDateTextColor();
        }

        @Override
        public int clickableTextColor (boolean isPressed) {
          return ColorUtils.fromToArgb(
            getBubbleDateTextColor(),
            Theme.getColor(R.id.theme_color_messageAuthor),
            messagesController().wallpaper().getBackgroundTransparency()
          );
        }

        @Override
        public int backgroundColor (boolean isPressed) {
          int colorId = backgroundColorId(isPressed);
          return colorId != 0 ?
            Theme.getColor(colorId) :
            0;
        }

        @Override
        public int backgroundColorId (boolean isPressed) {
          float transparency = messagesController().wallpaper().getBackgroundTransparency();
          return isPressed && transparency == 1f ?
            R.id.theme_color_messageAuthor :
            0;
        }
      };
    } else {
      return new TextColorSet() {
        @Override
        public int defaultTextColor () {
          return getBubbleDateTextColor();
        }

        @Override
        public int clickableTextColor (boolean isPressed) {
          return Theme.getColor(R.id.theme_color_messageAuthor);
        }
      };
    }
  }
}