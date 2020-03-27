package com.fondova.finance.vo;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverter;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

@Entity
public class User implements Parcelable {

    @ColumnInfo(name = "_id")
    @PrimaryKey(autoGenerate = true)
    public long id;
    @SerializedName("info")
    @Expose
    public String info;
    @SerializedName("username")
    @Expose
    public String username;
    @SerializedName("whoId")
    @Expose
    public int whoId;
    @SerializedName("wspVersion")
    @Expose
    public String wspVersion;
    @SerializedName("protocolRevision")
    @Expose
    public String protocolRevision;
    @SerializedName("qsVersion")
    @Expose
    public String qsVersion;
    @SerializedName("dataSrc")
    @Expose
    public String dataSrc;

    @ColumnInfo(name = "is_logged_in")
    public boolean isLoggedIn;

    @ColumnInfo(name = "accepted_eula")
    public boolean acceptedEula = false;

    @ColumnInfo(name = "sync_with_drive")
    public boolean syncWithDrive = false;

    @ColumnInfo(name = "asked_sync_with_drive")
    public boolean askedSyncWithDrive = false;

    @SerializedName("features")
    @Expose
    public Features features;


    public class UserFeatureTypeConverter {
        private Gson gson = new Gson();

        @TypeConverter
        public String TodefaultFeature(Features dEFAULTFEATURE) {
            return gson.toJson(dEFAULTFEATURE.toString());
        }
        @TypeConverter
        public Features getdefaultFeature(String json) {
            return gson.fromJson(json, new TypeToken<Features>(){}.getType());
        }

    }

    public User() {
    }


    protected User(Parcel in) {
        id = in.readLong();
        info = in.readString();
        username = in.readString();
        whoId = in.readInt();
        wspVersion = in.readString();
        protocolRevision = in.readString();
        qsVersion = in.readString();
        dataSrc = in.readString();
        isLoggedIn = in.readByte() != 0;
        acceptedEula = in.readByte() != 0;
        syncWithDrive = in.readByte() != 0;
        askedSyncWithDrive = in.readByte() != 0;
        features = in.readParcelable(Features.class.getClassLoader());
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public Features getFeatures() {
        return features;
    }

    public void setFeatures(Features features) {
        this.features = features;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", info='" + info + '\'' +
                ", username='" + username + '\'' +
                ", whoId=" + whoId +
                ", wspVersion='" + wspVersion + '\'' +
                ", protocolRevision='" + protocolRevision + '\'' +
                ", qsVersion='" + qsVersion + '\'' +
                ", dataSrc='" + dataSrc + '\'' +
                ", isLoggedIn=" + isLoggedIn +
                ", acceptedEula=" + acceptedEula +
                ", syncWithDrive=" + syncWithDrive +
                ", askedSyncWithDrive=" + askedSyncWithDrive +
                ", OPTIONS_SERIES_VIEW= " + features.oPTIONSSERIESVIEW+
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeString(info);
        parcel.writeString(username);
        parcel.writeInt(whoId);
        parcel.writeString(wspVersion);
        parcel.writeString(protocolRevision);
        parcel.writeString(qsVersion);
        parcel.writeString(dataSrc);
        parcel.writeByte((byte) (isLoggedIn ? 1 : 0));
        parcel.writeByte((byte) (acceptedEula ? 1 : 0));
        parcel.writeByte((byte) (syncWithDrive ? 1 : 0));
        parcel.writeByte((byte) (askedSyncWithDrive ? 1 : 0));
        parcel.writeParcelable(features, i);
    }

    @Entity
    public class Features implements Parcelable {
        @SerializedName("DEFAULT_FEATURE")
        @Expose
        private Boolean dEFAULTFEATURE;

        @SerializedName("OPTIONS_SERIES_VIEW")
        @Expose
        private Boolean oPTIONSSERIESVIEW;

        public Features() {

        }

        protected Features(Parcel in) {
            byte tmpDEFAULTFEATURE = in.readByte();
            dEFAULTFEATURE = tmpDEFAULTFEATURE == 0 ? null : tmpDEFAULTFEATURE == 1;
            byte tmpOPTIONSSERIESVIEW = in.readByte();
            oPTIONSSERIESVIEW = tmpOPTIONSSERIESVIEW == 0 ? null : tmpOPTIONSSERIESVIEW == 1;
        }

        public final Creator<Features> CREATOR = new Creator<Features>() {
            @Override
            public Features createFromParcel(Parcel in) {
                return new Features(in);
            }

            @Override
            public Features[] newArray(int size) {
                return new Features[size];
            }
        };

        public Boolean getDEFAULTFEATURE() {
            return dEFAULTFEATURE;
        }

        public void setDEFAULTFEATURE(Boolean dEFAULTFEATURE) {
            this.dEFAULTFEATURE = dEFAULTFEATURE;
        }

        public Boolean getOPTIONSSERIESVIEW() {
            return oPTIONSSERIESVIEW;
        }

        public void setOPTIONSSERIESVIEW(Boolean oPTIONSSERIESVIEW) {
            this.oPTIONSSERIESVIEW = oPTIONSSERIESVIEW;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeByte((byte) (dEFAULTFEATURE == null ? 0 : dEFAULTFEATURE ? 1 : 2));
            parcel.writeByte((byte) (oPTIONSSERIESVIEW == null ? 0 : oPTIONSSERIESVIEW ? 1 : 2));
        }
    }
}
