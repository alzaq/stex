package cz.uhk.stex;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import cz.uhk.stex.model.Group;
import cz.uhk.stex.model.User;
import cz.uhk.stex.util.Promise;
import cz.uhk.stex.util.PromiseImpl;

/**
 * Holds all information about a logged in user
 */
public class LoggedInUser {
    private final @NotNull FirebaseUser firebaseUser;
    private @NotNull User user;
    private @NotNull Group activeGroup;
    private final List<Group> groups;
    private final List<UserChangeListener> userChangeListeners = new ArrayList<>();

    private static @Nullable Promise<LoggedInUser> instance;

    private LoggedInUser(@NotNull FirebaseUser firebaseUser, @NotNull User user, @NotNull Group activeGroup, List<Group> groups) {
        this.firebaseUser = firebaseUser;
        this.user = user;
        this.activeGroup = activeGroup;
        this.groups = groups;
    }

    private static Promise<FirebaseUser> resolveFirebaseUser() {
        final PromiseImpl<FirebaseUser> res = new PromiseImpl<>();
        FirebaseAuth.getInstance().addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null) {
                    res.resolve(firebaseAuth.getCurrentUser());
                }
            }
        });
        return res;
    }

    private static void listenToUserChanges(final LoggedInUser user) {
        Database.addUserChangeListener(user.getUser().getId(), new Database.UserChangeListener() {
            @Override
            public void userChanged(User newUser) {
                user.user = newUser;
                updateGroup(user);
                for (UserChangeListener userChangeListener : user.userChangeListeners) {
                    userChangeListener.userChanged(user);
                }
            }
        });
    }

    private static void updateGroup(final LoggedInUser user) {
        loadGroup(user.getUser()).success(new Promise.SuccessListener<Group, Void>() {
            @Override
            public Void onSuccess(Group result) throws Exception {
                if (result != null)
                    user.activeGroup = result;
                return null;
            }
        });
    }

    private static Promise<Group> loadGroup(User user) {
        String settingsGroupId = user.getActiveGroup();

        if (settingsGroupId == null)
            return Database.getFirstUserGroup(user.getId());
        else
            return Database.getGroupById(settingsGroupId);
    }

    private static Promise<LoggedInUser> initialize() {
        class UserParts {
            private FirebaseUser fbUser;
            private User user;
            private Group group;
            private List<Group> groups = new ArrayList<>();
        }

        return resolveFirebaseUser().success(new Promise.SuccessListener<FirebaseUser, UserParts>() {
            @Override
            public UserParts onSuccess(FirebaseUser fbUser) {
                UserParts res = new UserParts();
                res.fbUser = fbUser;
                return res;
            }
        }).successFlat(new Promise.SuccessListener<UserParts, Promise<UserParts>>() {
            @Override
            public Promise<UserParts> onSuccess(final UserParts userParts) {
                return Database.getUserById(userParts.fbUser.getUid()).success(new Promise.SuccessListener<User, UserParts>() {
                    @Override
                    public UserParts onSuccess(User result) {
                        userParts.user = result;
                        return userParts;
                    }
                });
            }
        }).successFlat(new Promise.SuccessListener<UserParts, Promise<UserParts>>() {
            @Override
            public Promise<UserParts> onSuccess(final UserParts userParts) throws Exception {
                List<Promise<Group>> groupPromises = new ArrayList<>();
                for (String s : userParts.user.getGroups()) {
                    groupPromises.add(Database.getGroupById(s));
                }

                return Promise.all(groupPromises).success(new Promise.SuccessListener<List<Group>, UserParts>() {
                    @Override
                    public UserParts onSuccess(List<Group> result) throws Exception {
                        userParts.groups = result;
                        return userParts;
                    }
                });
            }
        }).successFlat(new Promise.SuccessListener<UserParts, Promise<UserParts>>() {
            @Override
            public Promise<UserParts> onSuccess(final UserParts userParts) {
                Promise.SuccessListener<Group, UserParts> setGroup = new Promise.SuccessListener<Group, UserParts>() {
                    @Override
                    public UserParts onSuccess(Group result) throws Exception {
                        userParts.group = result;
                        return userParts;
                    }
                };
                return loadGroup(userParts.user).success(setGroup);
            }
        }).success(new Promise.SuccessListener<UserParts, LoggedInUser>() {
            @Override
            public LoggedInUser onSuccess(UserParts result) throws Exception {
                return new LoggedInUser(result.fbUser, result.user, result.group, result.groups);
            }
        }).success(new Promise.SuccessListener<LoggedInUser, LoggedInUser>() {
            @Override
            public LoggedInUser onSuccess(LoggedInUser result) throws Exception {
                listenToUserChanges(result);
                return result;
            }
        });
    }

    /**
     * Returns promise to the logged in user. The promise becomes resolved
     * as soon as all user info is downloaded.
     * @return logged in user
     */
    public static Promise<LoggedInUser> get() {
        if (instance != null) {
            return instance;
        } else {
            instance = initialize();
            return instance;
        }
    }

    /**
     * Clears instance.
     * Use for logout.
     */
    public static void clear() {
        instance = null;
    }

    @NotNull
    public FirebaseUser getFirebaseUser() {
        return firebaseUser;
    }

    @NotNull
    public User getUser() {
        return user;
    }

    @NotNull
    public Group getActiveGroup() {
        return activeGroup;
    }

    public List<Group> getGroups() {
        return groups;
    }

    /**
     * Listen to user group change events
     * @param listener listener
     */
    public void addUserChangeListener(@NotNull UserChangeListener listener) {
        userChangeListeners.add(listener);
    }

    public interface UserChangeListener {
        void userChanged(LoggedInUser newUser);
    }
}
