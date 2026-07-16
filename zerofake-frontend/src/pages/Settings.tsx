import { useState } from "react";
import { 
  Settings as SettingsIcon, 
  User, 
  Shield, 
  Bell, 
  Database, 
  Save, 
  RefreshCw 
} from "lucide-react";
import { toast } from "react-hot-toast";
import { useAuth } from "@/hooks/useAuth";
import {
  GlassBadge,
  GlassButton,
  GlassCard,
  GlassInput,
  GlassSelect
} from "@/components/ui";

export default function Settings() {
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState<"profile" | "blockchain" | "notifications">("profile");

  // Mock settings state initialized with sensible values
  const [profileForm, setProfileForm] = useState({
    firstName: user?.firstName || "John",
    lastName: user?.lastName || "Doe",
    email: user?.email || "manufacturer@zerofake.com",
    organization: "ZeroFake Manufacturing Corp"
  });

  const [blockchainForm, setBlockchainForm] = useState({
    channelName: "mychannel",
    chaincodeName: "zerofake",
    peerEndpoint: "localhost:7051",
    mspId: "Org1MSP"
  });

  const [notificationForm, setNotificationForm] = useState({
    emailAlerts: "immediate",
    fraudAlerts: true,
    systemUpdates: false
  });

  const handleSaveProfile = (e: React.FormEvent) => {
    e.preventDefault();
    toast.success("Profile settings updated successfully.");
  };

  const handleSaveBlockchain = (e: React.FormEvent) => {
    e.preventDefault();
    toast.success("Blockchain gateway configurations applied.");
  };

  const handleSaveNotifications = (e: React.FormEvent) => {
    e.preventDefault();
    toast.success("Notification preferences saved.");
  };

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-4xl font-bold bg-gradient-to-r from-white via-gray-200 to-gray-500 bg-clip-text text-transparent flex items-center gap-3">
          <SettingsIcon size={36} className="text-gray-300" />
          Settings
        </h1>
        <p className="mt-2 text-gray-400">
          Configure your platform profile, notification subscriptions, and Hyperledger Fabric properties.
        </p>
      </div>

      <div className="grid gap-8 md:grid-cols-4">
        {/* Navigation Sidebar Tabs */}
        <div className="md:col-span-1 flex flex-col gap-2">
          <GlassButton
            variant={activeTab === "profile" ? "primary" : "secondary"}
            onClick={() => setActiveTab("profile")}
            className="justify-start gap-3 w-full"
          >
            <User size={18} />
            Profile Info
          </GlassButton>
          
          <GlassButton
            variant={activeTab === "blockchain" ? "primary" : "secondary"}
            onClick={() => setActiveTab("blockchain")}
            className="justify-start gap-3 w-full"
          >
            <Database size={18} />
            Fabric Config
          </GlassButton>

          <GlassButton
            variant={activeTab === "notifications" ? "primary" : "secondary"}
            onClick={() => setActiveTab("notifications")}
            className="justify-start gap-3 w-full"
          >
            <Bell size={18} />
            Notifications
          </GlassButton>
        </div>

        {/* Form Settings View */}
        <div className="md:col-span-3">
          {activeTab === "profile" && (
            <GlassCard>
              <form onSubmit={handleSaveProfile} className="space-y-6">
                <div>
                  <h2 className="text-xl font-bold text-white flex items-center gap-2">
                    <User size={20} className="text-blue-400" />
                    Profile Details
                  </h2>
                  <p className="text-sm text-gray-400 mt-1">Manage your administrator profile details.</p>
                </div>

                <div className="grid gap-4 sm:grid-cols-2">
                  <GlassInput
                    label="First Name"
                    value={profileForm.firstName}
                    onChange={(e) => setProfileForm({ ...profileForm, firstName: e.target.value })}
                  />
                  <GlassInput
                    label="Last Name"
                    value={profileForm.lastName}
                    onChange={(e) => setProfileForm({ ...profileForm, lastName: e.target.value })}
                  />
                  <div className="sm:col-span-2">
                    <GlassInput
                      label="User ID"
                      value={user?.id || ""}
                      disabled
                      readOnly
                      helperText="Your immutable platform user identifier (UUID)."
                    />
                  </div>
                  <div className="sm:col-span-2">
                    <GlassInput
                      label="Email Address"
                      type="email"
                      value={profileForm.email}
                      onChange={(e) => setProfileForm({ ...profileForm, email: e.target.value })}
                      disabled
                    />
                  </div>
                  <div className="sm:col-span-2">
                    <GlassInput
                      label="Organization"
                      value={profileForm.organization}
                      onChange={(e) => setProfileForm({ ...profileForm, organization: e.target.value })}
                    />
                  </div>
                </div>

                <div className="pt-4 border-t border-white/5 flex justify-end">
                  <GlassButton type="submit" className="flex items-center gap-2">
                    <Save size={18} />
                    Save Profile
                  </GlassButton>
                </div>
              </form>
            </GlassCard>
          )}

          {activeTab === "blockchain" && (
            <GlassCard>
              <form onSubmit={handleSaveBlockchain} className="space-y-6">
                <div>
                  <div className="flex items-center justify-between">
                    <h2 className="text-xl font-bold text-white flex items-center gap-2">
                      <Shield size={20} className="text-indigo-400" />
                      Fabric Connection Gateway
                    </h2>
                    <GlassBadge variant="success">CONNECTED</GlassBadge>
                  </div>
                  <p className="text-sm text-gray-400 mt-1">Properties for connection validation.</p>
                </div>

                <div className="grid gap-4 sm:grid-cols-2">
                  <GlassInput
                    label="Channel Name"
                    value={blockchainForm.channelName}
                    onChange={(e) => setBlockchainForm({ ...blockchainForm, channelName: e.target.value })}
                  />
                  <GlassInput
                    label="Chaincode ID"
                    value={blockchainForm.chaincodeName}
                    onChange={(e) => setBlockchainForm({ ...blockchainForm, chaincodeName: e.target.value })}
                  />
                  <GlassInput
                    label="Peer Endorsement Node Endpoint"
                    value={blockchainForm.peerEndpoint}
                    onChange={(e) => setBlockchainForm({ ...blockchainForm, peerEndpoint: e.target.value })}
                  />
                  <GlassInput
                    label="MSP Provider ID"
                    value={blockchainForm.mspId}
                    onChange={(e) => setBlockchainForm({ ...blockchainForm, mspId: e.target.value })}
                  />
                </div>

                <div className="pt-4 border-t border-white/5 flex justify-end gap-3">
                  <GlassButton variant="secondary" className="flex items-center gap-2">
                    <RefreshCw size={16} />
                    Test Connection
                  </GlassButton>
                  <GlassButton type="submit" className="flex items-center gap-2">
                    <Save size={18} />
                    Apply Properties
                  </GlassButton>
                </div>
              </form>
            </GlassCard>
          )}

          {activeTab === "notifications" && (
            <GlassCard>
              <form onSubmit={handleSaveNotifications} className="space-y-6">
                <div>
                  <h2 className="text-xl font-bold text-white flex items-center gap-2">
                    <Bell size={20} className="text-purple-400" />
                    Alert Notifications
                  </h2>
                  <p className="text-sm text-gray-400 mt-1">Configure email notification rules.</p>
                </div>

                <div className="space-y-4">
                  <GlassSelect
                    label="Security Alerts Digest"
                    value={notificationForm.emailAlerts}
                    onChange={(e) => setNotificationForm({ ...notificationForm, emailAlerts: e.target.value })}
                    options={[
                      { label: "Immediate on Incident", value: "immediate" },
                      { label: "Daily Summary Log", value: "daily" },
                      { label: "Weekly Audit Digest", value: "weekly" },
                      { label: "Mute All Email Alerts", value: "mute" }
                    ]}
                  />

                  <div className="space-y-3 pt-3">
                    <label className="flex items-center gap-3 cursor-pointer">
                      <input 
                        type="checkbox" 
                        checked={notificationForm.fraudAlerts}
                        onChange={(e) => setNotificationForm({ ...notificationForm, fraudAlerts: e.target.checked })}
                        className="rounded text-indigo-500 bg-white/5 border-white/10 focus:ring-indigo-500" 
                      />
                      <span className="text-sm text-gray-300 font-medium">Email me when high-risk fraud triggers occur</span>
                    </label>

                    <label className="flex items-center gap-3 cursor-pointer">
                      <input 
                        type="checkbox" 
                        checked={notificationForm.systemUpdates}
                        onChange={(e) => setNotificationForm({ ...notificationForm, systemUpdates: e.target.checked })}
                        className="rounded text-indigo-500 bg-white/5 border-white/10 focus:ring-indigo-500" 
                      />
                      <span className="text-sm text-gray-300 font-medium">Send monthly blockchain platform optimization reports</span>
                    </label>
                  </div>
                </div>

                <div className="pt-4 border-t border-white/5 flex justify-end">
                  <GlassButton type="submit" className="flex items-center gap-2">
                    <Save size={18} />
                    Save Preferences
                  </GlassButton>
                </div>
              </form>
            </GlassCard>
          )}
        </div>
      </div>
    </div>
  );
}
