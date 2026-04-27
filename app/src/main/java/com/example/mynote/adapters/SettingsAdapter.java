package com.example.mynote.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mynote.R;
import com.example.mynote.models.SettingItem;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import com.example.mynote.models.User;
import android.view.View;
public class SettingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnSettingClickListener {
        void onTrashClick();
        void onLogoutClick();
        void onAccount1Click();
        void onAccount2Click();
        void onAddAccountClick();
        void onProfileClick();
    }

    private final List<SettingItem> items;
    private final OnSettingClickListener listener;

    public SettingsAdapter(List<SettingItem> items, OnSettingClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == SettingItem.TYPE_PROFILE_CARD) {
            View view = inflater.inflate(R.layout.item_settings_profile_card, parent, false);
            return new ProfileViewHolder(view);
        } else if (viewType == SettingItem.TYPE_MENU_CARD) {
            View view = inflater.inflate(R.layout.item_settings_menu_card, parent, false);
            return new MenuViewHolder(view);
        } else if (viewType == SettingItem.TYPE_SECTION_TITLE) {
            View view = inflater.inflate(R.layout.item_settings_section_title, parent, false);
            return new SectionViewHolder(view);
        } else if (viewType == SettingItem.TYPE_ACCOUNT_CARD) {
            View view = inflater.inflate(R.layout.item_settings_account_card, parent, false);
            return new AccountViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_settings_add_button, parent, false);
            return new AddButtonViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        SettingItem item = items.get(position);

        if (holder instanceof ProfileViewHolder) {
            ProfileViewHolder h = (ProfileViewHolder) holder;
            h.tvName.setText(item.getTitle());
            h.tvEmail.setText(item.getSubtitle());
            h.itemView.setOnClickListener(v -> listener.onProfileClick());

        } else if (holder instanceof SectionViewHolder) {
            ((SectionViewHolder) holder).tvSectionTitle.setText(item.getTitle());

        } else if (holder instanceof MenuViewHolder) {
            MenuViewHolder h = (MenuViewHolder) holder;
            h.layoutTrash.setOnClickListener(v -> listener.onTrashClick());
            h.layoutLogout.setOnClickListener(v -> listener.onLogoutClick());

        } else if (holder instanceof AccountViewHolder) {
            AccountViewHolder h = (AccountViewHolder) holder;

            User account1 = item.getAccount1();
            User account2 = item.getAccount2();

            if (account1 != null) {
                h.layoutAccount1.setVisibility(View.VISIBLE);
                h.tvAccountName1.setText(
                        account1.getFullName() != null && !account1.getFullName().isEmpty()
                                ? account1.getFullName()
                                : "Người dùng"
                );
                h.tvAccountEmail1.setText(
                        account1.getEmail() != null ? account1.getEmail() : ""
                );
                h.layoutAccount1.setOnClickListener(v -> listener.onAccount1Click());
            } else {
                h.layoutAccount1.setVisibility(View.GONE);
            }

            if (account2 != null) {
                h.layoutAccount2.setVisibility(View.VISIBLE);
                h.tvAccountName2.setText(
                        account2.getFullName() != null && !account2.getFullName().isEmpty()
                                ? account2.getFullName()
                                : "Người dùng"
                );
                h.tvAccountEmail2.setText(
                        account2.getEmail() != null ? account2.getEmail() : ""
                );
                h.layoutAccount2.setOnClickListener(v -> listener.onAccount2Click());
            } else {
                h.layoutAccount2.setVisibility(View.GONE);
            }

        } else if (holder instanceof AddButtonViewHolder) {
            ((AddButtonViewHolder) holder).layoutAddAccount.setOnClickListener(
                    v -> listener.onAddAccountClick()
            );
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ProfileViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail;

        public ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
        }
    }

    static class MenuViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutTrash, layoutLogout;

        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutTrash = itemView.findViewById(R.id.layoutTrash);
            layoutLogout = itemView.findViewById(R.id.layoutLogout);
        }
    }

    static class SectionViewHolder extends RecyclerView.ViewHolder {
        TextView tvSectionTitle;

        public SectionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSectionTitle = itemView.findViewById(R.id.tvSectionTitle);
        }
    }

    static class AccountViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutAccount1, layoutAccount2;
        TextView tvAccountName1, tvAccountEmail1, tvAccountName2, tvAccountEmail2;

        public AccountViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutAccount1 = itemView.findViewById(R.id.layoutAccount1);
            layoutAccount2 = itemView.findViewById(R.id.layoutAccount2);
            tvAccountName1 = itemView.findViewById(R.id.tvAccountName1);
            tvAccountEmail1 = itemView.findViewById(R.id.tvAccountEmail1);
            tvAccountName2 = itemView.findViewById(R.id.tvAccountName2);
            tvAccountEmail2 = itemView.findViewById(R.id.tvAccountEmail2);
        }
    }

    static class AddButtonViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutAddAccount;

        public AddButtonViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutAddAccount = itemView.findViewById(R.id.layoutAddAccount);
        }
    }
}
