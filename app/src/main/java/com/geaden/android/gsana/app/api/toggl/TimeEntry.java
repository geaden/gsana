package com.geaden.android.gsana.app.api.toggl;

import com.geaden.android.gsana.app.api.toggl.util.DateUtil;

import org.json.simple.JSONObject;

import java.util.Date;


/**
 * Toggl time entry.
 */
public class TimeEntry {
    private Long id;
    private String description;
    private Date start;
    private Date stop;
    private long duration;
    private Boolean billable;
    private String created_with;
    private Boolean duronly;
    private Long pid;
    private Long wid;
    private Long tid;

    public TimeEntry() {
        this.created_with = "Gsana Toggl Client";
    }

    public TimeEntry(JSONObject object) {
        this();
        this.id = (Long) object.get("id");
        this.description = (String) object.get("description");
        this.start = DateUtil.convertStringToDate((String) object.get("start"));
        this.stop = DateUtil.convertStringToDate((String) object.get("stop"));
        this.duration = (Long) object.get("duration");
        this.billable = (Boolean) object.get("billable");
        this.duronly = (Boolean) object.get("duronly");
        created_with = (String) object.get("created_with");
        this.pid = (Long) object.get("pid");
        this.wid = (Long) object.get("wid");
        this.tid = (Long) object.get("tid");
    }

    public Boolean isBillable() {
        return billable;
    }

    public void setBillable(Boolean billable) {
        this.billable = billable;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getDuronly() {
        return duronly;
    }

    public void setDuronly(Boolean duronly) {
        this.duronly = duronly;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getStop() {
        return stop;
    }

    public void setStop(Date stop) {
        this.stop = stop;
    }

    public String getCreated_with() {
        return created_with;
    }

    public void setCreated_with(String created_with) {
        this.created_with = created_with;
    }

    public Long getWid() {
        return wid;
    }

    public void setWid(Long wid) {
        this.wid = wid;
    }

    public Long getPid() {
        return pid;
    }

    public void setPid(Long pid) {
        this.pid = pid;
    }

    public Long getTid() {
        return tid;
    }

    public void setTid(Long tid) {
        this.tid = tid;
    }

    public JSONObject toJSONObject() {
        JSONObject object = new JSONObject();
        if (billable != null) {
            object.put("billable", billable);
        }
        if (description != null) {
            object.put("description", description);
        }
        if (duration != 0) {
            object.put("duration", duration);
        }
        if (id != null) {
            object.put("id", id);
        }
        if (duronly != null) {
            object.put("duronly", duronly);
        }
        if (start != null) {
            object.put("start", DateUtil.convertDateToString(start));
        }
        if (stop != null) {
            object.put("stop", DateUtil.convertDateToString(stop));
        }
        if (created_with != null) {
            object.put("created_with", created_with);
        }

        if (pid != null) {
            object.put("pid", this.pid);
        }

        if (wid != null) {
            object.put("wid", this.wid);
        }
        if (tid != null) {
            object.put("tid", this.tid);
        }
        JSONObject timeEntry = new JSONObject();
        timeEntry.put("time_entry", object);
        return timeEntry;
    }

    public String toJSONString() {
        return this.toJSONObject().toJSONString();
    }

    @Override
    public String toString() {
        return "TimeEntry{" + "id=" + id + ", description=" + description +
                ", start=" + start + ", stop=" + stop + ", duration=" + duration +
                ", billable=" + billable + ", duronly=" + duronly + ", tid = " + tid + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TimeEntry other = (TimeEntry) obj;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }
}
