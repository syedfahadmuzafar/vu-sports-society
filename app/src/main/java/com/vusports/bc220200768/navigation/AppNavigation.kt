package com.vusports.bc220200768.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.firebase.auth.FirebaseAuth
import com.vusports.bc220200768.screens.EventScreen
import com.vusports.bc220200768.screens.ProfileScreen
import com.vusports.bc220200768.screens.admin.AdminDashboardScreen
import com.vusports.bc220200768.screens.admin.ApproveResultsScreen
import com.vusports.bc220200768.screens.admin.ApproveTeamsEventsScreen
import com.vusports.bc220200768.screens.admin.ManageCategoriesScreen
import com.vusports.bc220200768.screens.admin.ManageUsersScreen
import com.vusports.bc220200768.screens.admin.ModifyProfilesScreen
import com.vusports.bc220200768.screens.admin.NotificationsScreen
import com.vusports.bc220200768.screens.admin.ScheduleEventScreen
import com.vusports.bc220200768.screens.admin.ViewReportsScreen
import com.vusports.bc220200768.screens.auth.ForgotPasswordScreen
import com.vusports.bc220200768.screens.auth.LoginScreen
import com.vusports.bc220200768.screens.coach.ApproveParticipantsScreen
import com.vusports.bc220200768.screens.coach.CoachDashboardScreen
import com.vusports.bc220200768.screens.coach.CreateEventScreen
import com.vusports.bc220200768.screens.coach.EventPerformanceScreen
import com.vusports.bc220200768.screens.coach.EventSelectorScreen
import com.vusports.bc220200768.screens.coach.OrganizeEventScreen
import com.vusports.bc220200768.screens.coach.ParticipantPointsScreen
import com.vusports.bc220200768.screens.coach.TeamManagementScreen
import com.vusports.bc220200768.screens.coach.TeamScreen
import com.vusports.bc220200768.screens.common.SplashScreen
import com.vusports.bc220200768.screens.leaderboard.LeaderboardScreen
import com.vusports.bc220200768.screens.participant.DashboardScreen
import com.vusports.bc220200768.screens.participant.JoinedEventsScreen
import com.vusports.bc220200768.screens.participant.PerformanceTrackingScreen
import com.vusports.bc220200768.screens.participant.SportTeamScreen
import com.vusports.bc220200768.screens.teams.ChatScreen
import com.vusports.bc220200768.screens.teams.ChatSelectorScreen
import com.vusportssociety.screens.auth.RegisterScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "splash") {

        composable("splash") { SplashScreen(navController) }
        composable("approveParticipants") { ApproveParticipantsScreen(navController) }

        composable("teamManagement") { TeamManagementScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("forgot_password") { ForgotPasswordScreen(navController) }

        composable("create_event") {
            val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
            CreateEventScreen(navController = navController, coachEmail = currentUserEmail)
        }



        composable("coachDashboard") { CoachDashboardScreen(navController) }
        composable("adminDashboard") { AdminDashboardScreen(navController) }
        composable("modify_profiles") { ModifyProfilesScreen(navController) }
        composable("manage_users") { ManageUsersScreen() }
        composable("approve_teams_events") { ApproveTeamsEventsScreen() }
        composable("approve_results") { ApproveResultsScreen(navController) }
        composable("schedule_events") { ScheduleEventScreen() }
        composable("send_notifications") {
            NotificationsScreen(
                navController
            )
        }
        composable("view_reports") { ViewReportsScreen() }
        composable("teams") { TeamScreen(navController) }
        composable("events") { EventScreen(navController) }
        composable("joined_events") {
            JoinedEventsScreen()
        }

        composable("leaderboard") {
            LeaderboardScreen()
        }

        composable("participant_points") {
            ParticipantPointsScreen(navController)
        }

        composable("event_selector") {
            EventSelectorScreen(navController)
        }

        composable("event_performance/{eventId}") { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            EventPerformanceScreen(navController, eventId)
        }
        
        composable("organize_events") {
            OrganizeEventScreen(navController)
        }
        
        composable("teamApproval") {
            com.vusports.bc220200768.screens.coach.TeamApprovalScreen(navController)
        }





        composable("userDashboard") { DashboardScreen(navController) }

        composable("chat/{chatType}/{id}/{userEmail}") { backStackEntry ->
            val chatType = backStackEntry.arguments?.getString("chatType") ?: "team"
            val id = backStackEntry.arguments?.getString("id") ?: "default"
            val email = backStackEntry.arguments?.getString("userEmail") ?: ""

            ChatScreen(navController, chatType = chatType, id = id, currentUserEmail = email)
        }
        composable("chat_selector/{userEmail}") { backStackEntry ->
            val email = backStackEntry.arguments?.getString("userEmail") ?: ""
            ChatSelectorScreen(navController, currentUserEmail = email)
        }


        composable("manage_categories") {
            ManageCategoriesScreen()
        }
        composable("profile") { ProfileScreen(navController) } // Profile screen route
        composable("performance_history") { PerformanceTrackingScreen(navController) } // Performance history screen route
        composable("sport_team") { SportTeamScreen(navController) } // Sport-specific team creation screen
    }
}

