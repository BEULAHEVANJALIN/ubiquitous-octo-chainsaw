package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.*;

import javax.annotation.Nonnull;

import com.google.common.graph.Graph;
import com.google.common.graph.ImmutableValueGraph;
import com.google.common.graph.ValueGraph;
import com.sun.jdi.connect.Transport;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.Piece.MrX;
import uk.ac.bris.cs.scotlandyard.model.Piece;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * cw-model
 * Stage 1: Complete this class
 */
public final class MyGameStateFactory implements Factory<GameState> {

	private final class MyGameState implements GameState {
		private GameSetup setup;
		private ImmutableSet<Piece> remaining;
		private ImmutableList<LogEntry> log;
		private Player mrX;
		private List<Player> detectives;
		private ImmutableSet<Move> moves;
		private ImmutableSet<Piece> winner;
		private List<Player> players;
		private MyGameState(
				final GameSetup setup,
				final ImmutableSet<Piece> remaining,
				final ImmutableList<LogEntry> log,
				final Player mrX,
				final List<Player> detectives) {
			this.setup = setup;
			this.remaining = remaining;
			this.log = log;
			this.mrX = mrX;
			this.detectives = detectives;
			this.players = Lists.asList(mrX, detectives.toArray(Player[]::new));
			this.winner = getWinner();
			this.moves = getAvailableMoves();

			if (setup.moves.isEmpty()) throw new IllegalArgumentException("Moves is empty!");

			//testNullDetectiveShouldThrow
			if(detectives.equals(null)) throw new NullPointerException("detectives should not be null");
			// size represents the number of detectives

			//testNullMrXShouldThrow
			if (mrX.equals(null)) throw new NullPointerException("mrX should not be empty");
			// should not be null
			// if it is null throw NullPointerException

			//testNoMrXShouldThrow
			if (!mrX.isMrX()) throw new IllegalArgumentException("There should be mrX");
			// so this boolen method originally returns either true or false.
			// with the exclimation mark it will return false.
			// throw an error

			//TestMoreThanOneMrXShouldThrow
			for (Player p: detectives) {
			if(p.isMrX()) throw new IllegalArgumentException("There should be one mrX");
			}
			for (Player p: detectives) {
				if (p.piece().isMrX()) throw new IllegalArgumentException("There should be one mrX");
			}

			//if (mrXcount>1) throw new IllegalArgumentException ("There should only be one mrX");
			// define mrX as zero first
			// mrXcount increases to one
			// Anything more than one mrXcount will throw an error


			//testDetectiveHaveDoubleTicketsShouldThrow
			for (Player p : detectives) {
				if (p.has(ScotlandYard.Ticket.DOUBLE)) throw new IllegalArgumentException("detectives should not have double tickets");
			}

			//testDetectiveHaveSecretTickets
			for (Player p : detectives) {
				if(p.has(ScotlandYard.Ticket.SECRET)) throw new IllegalArgumentException("detectives should not have secret tickets");
			}

			//testDuplicateDetectiveShouldThrow
			for (int i = 0; i < detectives.size(); i++) {
				for (int j = i+1; j < detectives.size(); j++) {
					if(detectives.get(i).equals(detectives.get(j))){
						throw new IllegalArgumentException("There should not be any duplicate detectives");
					}
				}
			}

			//testEmptyGraphShouldThrow
			if(setup.graph.nodes().isEmpty()) throw new IllegalArgumentException("Empty graph should not throw");

			//testLocationOverlapBetweenDetectivesShouldThrow
			//Set<Integer>
			for(Player p : detectives){
				//if((p.location) throw new IllegalArgumentException("Should not throw");
			}
		}

		@Nonnull
		@Override
		public GameSetup getSetup() {
			return setup;
		}

		@Nonnull
		@Override
		public ImmutableSet<Piece> getPlayers() {
			return ImmutableSet.copyOf(
					players
						.stream()
						.map(Player::piece)
						.toArray(Piece[]::new)

			);

		}

		@Nonnull
		@Override
		public Optional<Integer> getDetectiveLocation(Piece.Detective detective) {
			for (Player p : detectives) {
				if (p.piece().equals(detective)) {
					return Optional.of(p.location());
				}
			}
			return Optional.empty();
		}

		@Nonnull
		@Override
		public Optional<TicketBoard> getPlayerTickets(Piece piece) {
			for (Player p : players) {
				if (p.piece().equals(piece)) {
					return Optional.of(
							new TicketBoard() {
								@Override
								public int getCount(@Nonnull ScotlandYard.Ticket ticket) {
									return p.tickets().get(ticket);
								}
							}
					);
				}
			}
			return Optional.empty();
		}

		@Nonnull
		@Override
		public ImmutableList<LogEntry> getMrXTravelLog() {
			return log;
		}

		@Nonnull
		@Override
		public ImmutableSet<Piece> getWinner() {
			if (remaining.contains(mrX.piece())) {
				if (getMrxSinlgeMoves().isEmpty()) {
					return ImmutableSet.copyOf(detectives.stream().map(d -> d.piece()).collect(Collectors.toList()));
				}
				if (this.log.size() > 21 || getDetectiveSingleMoves().isEmpty()) {
					return ImmutableSet.of(mrX.piece());
				}
			} else {
				if (getDetectiveSingleMoves().isEmpty()) {
					return ImmutableSet.of(mrX.piece());
				}
				if (detectives.stream().map(d -> d.location()).collect(Collectors.toSet()).contains(mrX.location())
						|| getMrxSinlgeMoves().isEmpty()) {
					return ImmutableSet.copyOf(detectives.stream().map(d -> d.piece()).collect(Collectors.toList()));
				}
			}
			return ImmutableSet.of();
		}

		private static Set<Move.SingleMove> makeSingleMoves(GameSetup setup, List<Player> detectives, Player player, int source) {

			// TODO create an empty collection of some sort, say, HashSet, to store all the SingleMove we generate
			// Create a empty hash set
			Set<Move.SingleMove> h_set = new HashSet<>();

			for (int destination : setup.graph.adjacentNodes(source)) {
				boolean free = true;

				// TODO find out if destination is occupied by a detective
				// if the location is occupied, don't add to the collection of moves to return
				for (Player p : detectives) {
					if (p.location() == destination){
						free = false;
						break;
					}
				}
				if (!free) continue;


				for (ScotlandYard.Transport t : setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of())) {
					// TODO find out if the player has the required tickets
					//  if it does, construct a SingleMove and add it the collection of moves to return
					if (player.has(t.requiredTicket())) {
						h_set.add(new Move.SingleMove(player.piece(), source, t.requiredTicket(), destination));
					}
				}

				// TODO consider the rules of secret moves here
				//  add moves to the destination via a secret ticket if there are any left with the player
				if (player.has(ScotlandYard.Ticket.SECRET)) {
					h_set.add(new Move.SingleMove(player.piece(), source, ScotlandYard.Ticket.SECRET, destination));
				}
			}

			// TODO return the collection of moves
			return h_set;
		}

		private ImmutableList<Boolean> tail (ImmutableList<Boolean> list) {
			var builder = ImmutableList.<Boolean>builder();
			for (int i = 1; i < list.size(); i++) builder.add(list.get(i));
			return builder.build();
		}
		private Set<Move.SingleMove> getDetectiveSingleMoves() {
			Set<Move.SingleMove> singleMoves = new HashSet<>();

			for (Player detective : detectives) {
				if (remaining.contains(detective.piece())) {
					for (Move.SingleMove m : makeSingleMoves(setup, detectives, detective, detective.location())) {
						singleMoves.add(m);
					}
				}
			}
			return singleMoves;
		}
		private Set<Move.SingleMove> getMrxSinlgeMoves() {
			return makeSingleMoves(setup, detectives, mrX, mrX.location());
		}
		@Nonnull
		@Override
		public ImmutableSet<Move> getAvailableMoves() {
			if (!getWinner().isEmpty()) return ImmutableSet.of();
			Set<Move> moves = new HashSet<>();
			Set<Move.SingleMove> singleMoves = new HashSet<>();
			if (remaining.contains(mrX.piece())) {
				singleMoves = getMrxSinlgeMoves();
			} else {
				singleMoves = getDetectiveSingleMoves();
			}
			for (Move.SingleMove m : singleMoves) {
				moves.add(m);
				if (setup.moves.size() > 1 && mrX.hasAtLeast(ScotlandYard.Ticket.DOUBLE, 1)) {
					for (int destination : setup.graph.adjacentNodes(m.destination)) {
						boolean free = true;
						for (Player p : detectives) {
							if (p.location() == destination){
								free = false;
								break;
							}
						}
						if (!free) continue;

						if (remaining.contains(mrX.piece())) {
							for (ScotlandYard.Transport t : setup.graph.edgeValueOrDefault(m.destination, destination, ImmutableSet.of())) {
								// TODO find out if the player has the required tickets
								//  if it does, construct a SingleMove and add it the collection of moves to return

								if ((m.ticket.equals(t.requiredTicket()) && mrX.hasAtLeast(t.requiredTicket(), 2)) ||
										(!m.ticket.equals(t.requiredTicket()) && mrX.has(t.requiredTicket()))) {
									moves.add(new Move.DoubleMove(mrX.piece(), m.source(), m.ticket, m.destination, t.requiredTicket(), destination));
								}
							}
							if (mrX.has(ScotlandYard.Ticket.SECRET)) {
								moves.add(new Move.DoubleMove(mrX.piece(), m.source(), m.ticket, m.destination, ScotlandYard.Ticket.SECRET, destination));
							}
						}
					}
				}

			}
			return ImmutableSet.copyOf(moves);
		}

		private ImmutableList<LogEntry> getEntry(Move.SingleMove move) {
			if (setup.moves.get(0)) {
				return ImmutableList.<LogEntry>builder()
						.addAll(log)
						.add(LogEntry.reveal(move.ticket, move.destination)).build();
			}
			return ImmutableList.<LogEntry>builder()
					.addAll(log)
					.add(LogEntry.hidden(move.ticket)).build();
		}


		private ImmutableList<LogEntry> getDoubleEntry(Move.DoubleMove move) {
			var builder = ImmutableList.<LogEntry>builder().addAll(log);
			if (setup.moves.get(0)) {
				builder.add(LogEntry.reveal(move.ticket1, move.destination1));
			} else {
				builder.add(LogEntry.hidden(move.ticket1));
			}
			if (setup.moves.get(1)) {
				builder.add(LogEntry.reveal(move.ticket2, move.destination2));
			} else {
				builder.add(LogEntry.hidden(move.ticket1));
			}
			return builder.build();
		}

		@Nonnull
		@Override
		public GameState advance(Move move) {
			if (!moves.contains(move)) throw new IllegalArgumentException("Illegal move: " + move);
			var visitor = new Move.Visitor<GameState>() {
				public GameState visit(Move.SingleMove m) {
					var p = m.commencedBy();
					if (p.isMrX()) {
						return new MyGameState(
								new GameSetup(setup.graph, tail(setup.moves)),
								ImmutableSet.copyOf(detectives.stream().map(d -> d.piece()).collect(Collectors.toList())),
								getEntry(m),
								mrX.use(m.ticket).at(m.destination),detectives
						);
					}

					detectives = detectives.stream().map(
							d -> {
								if (d.piece().equals(m.commencedBy()) && remaining.contains(d.piece())) {
									return d.use(m.ticket).at(m.destination);
								} else {
									return d;
								}
							}
					).collect(Collectors.toList());
					var nR = ImmutableSet.copyOf(remaining.stream().filter(piece -> !piece.equals(m.commencedBy()))
							.collect(Collectors.toList()));
					if (nR.size() != remaining.size() - 1) {
						System.out.println("Something wrong");
					}
					return new MyGameState(new GameSetup(setup.graph, tail(setup.moves)), nR, log, mrX, detectives);
				}

				public GameState visit(Move.DoubleMove m) {
					var p = m.commencedBy();
					if (!p.isMrX()) throw new IllegalArgumentException("Illegal move: " + move);
					return new MyGameState(
							new GameSetup(setup.graph, tail(tail(setup.moves))),
							ImmutableSet.copyOf(detectives.stream().map(d -> d.piece()).collect(Collectors.toList())),
							getDoubleEntry(m),
							mrX.use(m.ticket1).use(m.ticket2).at(m.destination2),detectives
					);
				}
			};
			return move.accept(visitor);
		}}

	@Nonnull
	@Override
	public GameState build(
			GameSetup setup,
			Player mrX,
			ImmutableList<Player> detectives) {

		// TODO
//		throw new RuntimeException("Implement me!");
		HashSet<Integer> locations = new HashSet<>();
		locations.add(mrX.location());

		detectives
				.stream()
				.forEach(p -> locations.add(p.location()));
		if (locations.size() != detectives.size() + 1) {
			throw new IllegalArgumentException("Pieces of the players should be in distinct location");
		}

		return new MyGameState(setup, ImmutableSet.of(MrX.MRX), ImmutableList.of(), mrX, detectives);

	}
}




