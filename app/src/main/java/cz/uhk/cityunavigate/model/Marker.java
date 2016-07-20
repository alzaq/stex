package cz.uhk.cityunavigate.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.List;

/**
 * Marker DTO
 */
@IgnoreExtraProperties
public class Marker implements Identifiable {
    private String id, idGroup, idUserAuthor, idCategory;
    private LatLng location;
    private List<String> commentIds;
    private String title, text;

    // For Firebase
    public Marker() {

    }

    private Marker(Builder builder) {
        id = builder.id;
        idGroup = builder.idGroup;
        idUserAuthor = builder.idUserAuthor;
        idCategory = builder.idCategory;
        location = builder.location;
        commentIds = builder.commentIds;
        title = builder.title;
        text = builder.text;
    }

    public static Builder builder(Marker copy) {
        Builder builder = new Builder();
        builder.text = copy.text;
        builder.title = copy.title;
        builder.commentIds = copy.commentIds;
        builder.location = copy.location;
        builder.idCategory = copy.idCategory;
        builder.idUserAuthor = copy.idUserAuthor;
        builder.idGroup = copy.idGroup;
        builder.id = copy.id;
        return builder;
    }

    public static IId builder() {
        return new Builder();
    }


    public String getId() {
        return id;
    }

    public String getIdGroup() {
        return idGroup;
    }

    public String getIdUserAuthor() {
        return idUserAuthor;
    }

    public LatLng getLocation() {
        return location;
    }

    public List<String> getCommentIds() {
        return commentIds;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public String getIdCategory() {
        return idCategory;
    }

    public interface IBuild {
        Marker build();
    }

    public interface IText {
        IBuild withText(String val);
    }

    public interface ITitle {
        IText withTitle(String val);
    }

    public interface ICommentIds {
        ITitle withCommentIds(List<String> val);
    }

    public interface ILocation {
        ICommentIds withLocation(LatLng val);
    }

    public interface IIdCategory {
        ILocation withIdCategory(String val);
    }

    public interface IIdUserAuthor {
        IIdCategory withIdUserAuthor(String val);
    }

    public interface IIdGroup {
        IIdUserAuthor withIdGroup(String val);
    }

    public interface IId {
        IIdGroup withId(String val);
    }

    public static final class Builder implements IText, ITitle, ICommentIds, ILocation, IIdCategory, IIdUserAuthor, IIdGroup, IId, IBuild {
        private String text;
        private String title;
        private List<String> commentIds;
        private LatLng location;
        private String idCategory;
        private String idUserAuthor;
        private String idGroup;
        private String id;

        private Builder() {
        }

        @Override
        public IBuild withText(String val) {
            text = val;
            return this;
        }

        @Override
        public IText withTitle(String val) {
            title = val;
            return this;
        }

        @Override
        public ITitle withCommentIds(List<String> val) {
            commentIds = val;
            return this;
        }

        @Override
        public ICommentIds withLocation(LatLng val) {
            location = val;
            return this;
        }

        @Override
        public ILocation withIdCategory(String val) {
            idCategory = val;
            return this;
        }

        @Override
        public IIdCategory withIdUserAuthor(String val) {
            idUserAuthor = val;
            return this;
        }

        @Override
        public IIdUserAuthor withIdGroup(String val) {
            idGroup = val;
            return this;
        }

        @Override
        public IIdGroup withId(String val) {
            id = val;
            return this;
        }

        public Marker build() {
            return new Marker(this);
        }
    }
}
